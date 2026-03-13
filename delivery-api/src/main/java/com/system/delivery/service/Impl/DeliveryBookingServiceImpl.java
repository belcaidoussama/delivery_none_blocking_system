package com.system.delivery.service.Impl;

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

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private RQueueReactive<DeliveryBooking> bookingQueue;

    @PostConstruct
    private void init() {
        this.bookingQueue = redissonClient.getQueue("bookingQueue");
    }

    @PreDestroy
    private void shutdownExecutor() {
        log.info("Shutting down virtual thread executor");
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

    public Mono<Void> deleteDeliveryBooking(Long id) {
        log.info("Deleting booking with ID: {}", id);
        return bookingRepository.deleteById(id)
                .doOnSuccess(unused -> log.info("Booking deleted successfully"))
                .doOnError(error -> log.error("Error deleting booking", error));
    }

    @Override
    public Flux<DeliveryBooking> getAllDeliveryBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @CacheEvict(value = "bookings", key = "#bookingDTO.id")
    public Mono<DeliveryBooking> createDeliveryBooking(DeliveryBookingDTO bookingDTO) {
        return bookingQueue.offer(bookingDTO.toEntity())
                .then(processBookingQueue())
                .doOnSuccess(savedBooking -> log.info("Booking successfully created: {}", savedBooking))
                .doOnError(error -> log.error("Error creating booking", error));
    }

    private Mono<DeliveryBooking> processBookingQueue() {
        return bookingQueue.poll()
                .switchIfEmpty(Mono.empty())
                .flatMap(booking -> slotRepository.findById(booking.getSlotId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Slot not found")))
                        .flatMap(slot -> driverScheduleRepository.findByScheduleDateAndAvailable(booking.getDeliveryDate(), true)
                                .switchIfEmpty(Mono.error(new RuntimeException("No available drivers for this slot")))
                                .flatMap(schedule -> {
                                    RLockReactive lock = redissonClient.getLock("driver:" + schedule.getScheduleDate());
                                    return lock.tryLock()
                                            .flatMap(acquired -> {
                                                if (!acquired) {
                                                    return Mono.error(new RuntimeException("No available drivers for this slot lock"));
                                                }
                                                return driverScheduleRepository.updateAvailability(schedule.getId(), false)
                                                        .flatMap(rowsUpdated -> {
                                                            if (rowsUpdated <= 0) {
                                                                return Mono.error(new RuntimeException("Failed to update driver availability"));
                                                            }
                                                            return driverScheduleRepository.findById(schedule.getId());
                                                        })
                                                        .flatMap(updatedSchedule -> {
                                                            booking.setDeliveryDriverId(updatedSchedule.getDeliveryDriverId());
                                                            return bookingRepository.save(booking);
                                                        })
                                                        .doFinally(signal -> lock.unlock().subscribe());
                                            });
                                })
                        ))
                .publishOn(Schedulers.fromExecutor(virtualThreadExecutor));
    }

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

                    // Convert to Avro Schema Format
                    DeliveryStatusUpdate event = new DeliveryStatusUpdate(
                            updatedBooking.getId(),
                            updatedBooking.getStatus().toString(),
                            timestamp
                    );

                    // Send Kafka Event (Non-Blocking)
                    return Mono.fromFuture(() -> kafkaProducerService.sendStatusUpdate(event))
                            .thenReturn(updatedBooking);
                })
                .doOnSuccess(updatedBooking -> log.info("Booking status updated and Kafka event sent: {}", updatedBooking))
                .doOnError(error -> log.error("Error updating booking status", error));
    }
}
