package com.system.delivery.service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.system.delivery.producer.DeliveryStatusProducerService;
import com.system.delivery.schema.DeliveryStatusUpdate;
import org.redisson.api.RLockReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.DeliveryBookingDTO;
import com.system.delivery.dto.DeliveryStatusUpdateDTO;
import com.system.delivery.entity.DeliveryBooking;
import com.system.delivery.repository.DeliveryBookingRepository;
import com.system.delivery.repository.DeliverySlotRepository;
import com.system.delivery.repository.DriverScheduleRepository;
import com.system.delivery.service.DeliveryBookingService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryBookingServiceImpl implements DeliveryBookingService {

    private final DeliveryBookingRepository bookingRepository;
    private final DeliverySlotRepository slotRepository;
    private final DriverScheduleRepository driverScheduleRepository;
    private final RedissonReactiveClient redissonClient;
    private final DeliveryStatusProducerService kafkaProducerService;

    private RQueueReactive<DeliveryBooking> bookingQueue;

    @PostConstruct
    private void init() {
        this.bookingQueue = redissonClient.getQueue("bookingQueue");
        startQueueProcessor();
    }

    private void startQueueProcessor() {
        Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
                .flatMap(tick -> processBookingQueue())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    @Cacheable(value = "bookings", key = "#id")
    public Mono<DeliveryBooking> getDeliveryBookingById(Long id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id)
                .doOnSuccess(booking -> log.info("Booking found: {}", booking))
                .doOnError(error -> log.error("Error fetching booking", error));
    }

    @CacheEvict(value = "bookings", key = "#id")
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<Void> deleteDeliveryBooking(Long id) {
        log.info("Deleting booking with ID: {}", id);
        return bookingRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Booking deleted successfully"))
                .doOnError(error -> log.error("Error deleting booking", error));
    }

    @Override
    @Cacheable(value = "bookings")
    public Flux<DeliveryBooking> getAllDeliveryBookings() {
        return bookingRepository.findAll();
    }

    @CacheEvict(value = "bookings", allEntries = true)
    @Override
    public Mono<DeliveryBooking> createDeliveryBooking(DeliveryBookingDTO bookingDTO) {
        return bookingQueue.offer(bookingDTO.toEntity())
                .then(Mono.<DeliveryBooking>empty())
                .doOnSuccess(unused -> log.info("Booking successfully created: {}", bookingDTO))
                .doOnError(error -> log.error("Error creating booking", error));
    }

    private Mono<DeliveryBooking> processBookingQueue() {
        return Mono.defer(() -> bookingQueue.poll())
                .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofMillis(100)))
                .flatMap(booking -> slotRepository.findById(booking.getSlotId())
                        .flatMap(slot -> driverScheduleRepository.findByScheduleDateAndAvailable(booking.getDeliveryDate(), true)
                                .flatMap(schedule -> {
                                    String lockKey = "driver:" + DateTimeFormatter.ISO_LOCAL_DATE.format(schedule.getScheduleDate());
                                    RLockReactive lock = redissonClient.getLock(lockKey);
                                    return lock.tryLock()
                                            .flatMap(acquired -> {
                                                if (!acquired) {
                                                    return Mono.error(new IllegalStateException("Could not acquire lock"));
                                                }
                                                log.info("Lock ACQUIRED for driver: {} on date: {}", schedule.getDeliveryDriverId(), schedule.getScheduleDate());
                                                return driverScheduleRepository.updateAvailability(schedule.getId(), false)
                                                        .filter(rowsUpdated -> rowsUpdated > 0)
                                                        .flatMap(rows -> driverScheduleRepository.findById(schedule.getId()))
                                                        .switchIfEmpty(Mono.error(new RuntimeException("Failed to update driver availability")))
                                                        .flatMap(updatedSchedule -> {
                                                            Long driverId = updatedSchedule.getDeliveryDriverId();
                                                            if (driverId == null) {
                                                                return Mono.error(new IllegalStateException("No driver assigned to schedule"));
                                                            }
                                                            booking.setDeliveryDriverId(driverId);
                                                            return bookingRepository.save(booking)
                                                                    .doFinally(sig -> lock.unlock().subscribe());
                                                        });
                                            });
                                })
                        .switchIfEmpty(Mono.error(new RuntimeException("No available drivers for this slot"))));
    }

    @CacheEvict(value = "bookings", key = "#statusUpdateDTO.bookingId")
    @CacheEvict(value = "bookings", allEntries = true)
    @Override
    public Mono<DeliveryBooking> updateBookingStatus(DeliveryStatusUpdateDTO statusUpdateDTO) {
        return bookingRepository.findById(statusUpdateDTO.getBookingId())
                .switchIfEmpty(Mono.error(new RuntimeException("Booking not found")))
                .flatMap(existingBooking -> {
                    existingBooking.setStatus(statusUpdateDTO.getStatus());
                    return bookingRepository.save(existingBooking);
                })
                .flatMap(updatedBooking -> {
                    String timestamp = Instant.now().toString();
                    DeliveryStatusUpdate event = new DeliveryStatusUpdate(
                            updatedBooking.getId(),
                            updatedBooking.getStatus().toString(),
                            timestamp
                    );
                    return Mono.fromFuture(kafkaProducerService.sendStatusUpdate(event))
                            .doOnError(e -> log.error("Failed to send Kafka status update", e))
                            .onErrorResume(e -> Mono.empty())
                            .thenReturn(updatedBooking);
                })
                .doOnSuccess(updatedBooking -> log.info("Booking status updated and Kafka event sent: {}", updatedBooking))
                .doOnError(error -> log.error("Error updating booking status", error));
    }

}
