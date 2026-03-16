package com.system.delivery.service.impl;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.system.delivery.producer.DeliveryStatusProducerService;
import com.system.delivery.schema.DeliveryStatusUpdate;
import org.redisson.api.RLockReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.delivery.dto.DeliveryBookingDTO;
import com.system.delivery.dto.DeliveryStatusUpdateDTO;
import com.system.delivery.entity.DeliveryBooking;
import com.system.delivery.repository.DeliveryBookingRepository;
import com.system.delivery.repository.DeliverySlotRepository;
import com.system.delivery.repository.DriverScheduleRepository;
import com.system.delivery.service.DeliveryBookingService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryBookingServiceImpl implements DeliveryBookingService {

    private final DeliveryBookingRepository bookingRepository;
    private final DeliverySlotRepository slotRepository;
    private final DriverScheduleRepository driverScheduleRepository;
    private final RedissonReactiveClient redissonClient;
    private final DeliveryStatusProducerService kafkaProducerService;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private RQueueReactive<DeliveryBooking> bookingQueue;

    private static final String BOOKING_QUEUE = "bookingQueue";

    @PostConstruct
    private void init() {
        this.bookingQueue = redissonClient.getQueue(BOOKING_QUEUE);
    }

    @PreDestroy
    private void shutdownExecutor() {
        virtualThreadExecutor.shutdown();
    }

    @Override
    @Cacheable(value = "bookings", key = "#id")
    public Mono<DeliveryBooking> getDeliveryBookingById(Long id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id)
                .doOnSuccess(booking -> log.info("Booking found: {}", booking))
                .doOnError(error -> log.error("Error fetching booking", error));
    }

    @Override
    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<Void> deleteDeliveryBooking(Long id) {
        log.info("Deleting booking with ID: {}", id);
        return bookingRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Booking deleted successfully"))
                .doOnError(error -> log.error("Error deleting booking", error));
    }

    @Override
    @Cacheable(value = "bookings", key = "'all'")
    public Flux<DeliveryBooking> getAllDeliveryBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<DeliveryBooking> createDeliveryBooking(DeliveryBookingDTO bookingDTO) {
        return bookingQueue.offer(bookingDTO.toEntity())
                .thenReturn(bookingDTO.toEntity())
                .doOnSuccess(savedBooking -> log.info("Booking successfully enqueued: {}", savedBooking))
                .doOnError(error -> log.error("Error creating booking", error));
    }

    private Mono<DeliveryBooking> processBookingQueue() {
        return bookingQueue.poll()
                .doOnSuccess(b -> {
                    if (b == null) {
                        log.info("Booking queue is empty");
                    }
                })
                .flatMap(booking -> slotRepository.findById(booking.getSlotId())
                        .flatMap(slot -> driverScheduleRepository.findByScheduleDateAndAvailable(booking.getDeliveryDate(), true)
                                .flatMap(schedule -> {
                                    RLockReactive lock = redissonClient.getLock("driver:" + schedule.getScheduleDate());
                                    return lock.tryLock()
                                            .flatMap(acquired -> {
                                                if (!acquired) {
                                                    return Mono.error(new RuntimeException("No available drivers for this slot lock"));
                                                }
                                                log.info("Lock ACQUIRED for driver: {} on date: {}", schedule.getDeliveryDriverId(), schedule.getScheduleDate());
                                                return driverScheduleRepository.updateAvailability(schedule.getId(), false)
                                                        .flatMap(rowsUpdated -> rowsUpdated > 0 ? Mono.just(rowsUpdated) : Mono.error(new IllegalStateException("Driver schedule update failed")))
                                                        .flatMap(rows -> driverScheduleRepository.findById(schedule.getId()));
                                            })
                                            .doFinally(signal -> lock.unlock()
                                                    .doOnError(e -> log.warn("Failed to release lock", e))
                                                    .subscribe())
                                            .flatMap(updatedSchedule -> {
                                                booking.setDeliveryDriverId(updatedSchedule.getDeliveryDriverId());
                                                return bookingRepository.save(booking);
                                            });
                                }))
                        .switchIfEmpty(Mono.error(new RuntimeException("No available drivers for this slot"))));
    }

    @Override
    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<DeliveryBooking> updateBookingStatus(DeliveryStatusUpdateDTO statusUpdateDTO) {
        return bookingRepository.findById(statusUpdateDTO.getBookingId())
                .switchIfEmpty(Mono.error(new RuntimeException("Booking not found")))
                .flatMap(existingBooking -> {
                    existingBooking.setStatus(statusUpdateDTO.getStatus());
                    return bookingRepository.save(existingBooking);
                })
                .flatMap(updatedBooking -> {
                    DeliveryStatusUpdate event = toStatusUpdateEvent(updatedBooking);
                    kafkaProducerService.sendStatusUpdate(event).subscribe();
                    return Mono.just(updatedBooking);
                })
                .doOnSuccess(updatedBooking -> log.info("Booking status updated and Kafka event sent: {}", updatedBooking))
                .doOnError(error -> log.error("Error updating booking status", error));
    }

    private DeliveryStatusUpdate toStatusUpdateEvent(DeliveryBooking booking) {
        String timestamp = Instant.now().toString();
        return new DeliveryStatusUpdate(
                booking.getId(),
                booking.getStatus().toString(),
                timestamp
        );
    }
}
