package com.system.delivery.service.Impl;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.system.delivery.producer.DeliveryStatusProducerService;
import com.system.delivery.schema.DeliveryStatusUpdate;
import org.redisson.api.RLockReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.JsonCodec;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.DeliveryBookingDTO;
import com.system.delivery.dto.DeliveryStatusUpdateDTO;
import com.system.delivery.entity.DeliveryBooking;
import com.system.delivery.repository.DeliveryBookingRepository;
import com.system.delivery.repository.DeliverySlotRepository;
import com.system.delivery.repository.DriverScheduleRepository;
import com.system.delivery.service.DeliveryBookingService;

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

    // Initialize bookingQueue directly
    private final RQueueReactive<DeliveryBooking> bookingQueue =
            redissonClient.getQueue("deliveryBookingQueue", new JsonCodec<>(DeliveryBooking.class));

    // No longer need a virtual thread executor

    @Override
    @Cacheable(value = "bookings", key = "#id")
    public Mono<DeliveryBooking> getDeliveryBookingById(Long id) {
        log.info(" Fetching booking with ID: {}", id);
        return bookingRepository.findById(id)
                .doOnSuccess(booking -> log.info(" Booking found: {}", booking))
                .doOnError(error -> log.error("Error fetching booking", error));
    }

    @Override
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<Void> deleteDeliveryBooking(Long id) {
        log.info("Deleting booking with ID: {}", id);
        return bookingRepository.deleteById(id)
                .doOnSuccess(unused -> log.info(" Booking deleted successfully"))
                .doOnError(error -> log.error(" Error deleting booking", error));
    }

    @Override
    @Cacheable(value = "bookings")
    public Mono<List<DeliveryBooking>> getAllDeliveryBookings() {
        return bookingRepository.findAll()
                .collectList()
                .cache();
    }

    @Override
    @CacheEvict(value = "bookings", allEntries = true)
    public Mono<DeliveryBooking> createDeliveryBooking(DeliveryBookingDTO bookingDTO) {
        return bookingQueue.offer(bookingDTO.toEntity())
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return processBookingQueue();
                    } else {
                        return Mono.error(new IllegalStateException("Failed to enqueue booking"));
                    }
                })
                .doOnSuccess(savedBooking -> log.info("Booking successfully created: {}", savedBooking))
                .doOnError(error -> log.error(" Error creating booking", error));
    }

    private Mono<DeliveryBooking> processBookingQueue() {
        return Mono.defer(() -> bookingQueue.poll()
                .flatMap(booking -> slotRepository.findById(booking.getSlotId())
                        .flatMap(slot -> driverScheduleRepository.findByScheduleDateAndAvailable(booking.getDeliveryDate(), true)
                                .flatMap(schedule -> {
                                    RLockReactive lock = redissonClient.getLock("driver:" + schedule.getDeliveryDriverId() + ":" + schedule.getScheduleDate());
                                    return Mono.usingWhen(
                                            lock.tryLock(5, TimeUnit.SECONDS),
                                            acquired -> {
                                                if (!acquired) {
                                                    return Mono.error(new IllegalStateException("Lock not acquired"));
                                                }
                                                return driverScheduleRepository.updateAvailability(schedule.getId(), false)
                                                        .filter(rows -> rows > 0)
                                                        .switchIfEmpty(Mono.error(new OptimisticLockingFailureException("Schedule update failed")))
                                                        .flatMap(rows -> driverScheduleRepository.findById(schedule.getId()))
                                                        .flatMap(updatedSchedule -> {
                                                            booking.setDeliveryDriverId(updatedSchedule.getDeliveryDriverId());
                                                            return bookingRepository.save(booking);
                                                        });
                                            },
                                            lock -> lock.unlock(),
                                            (lock, ex) -> lock.unlock(),
                                            lock -> lock.unlock()
                                    );
                                })
                        )
                )
        )
        .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofMillis(100)))
        .next(); // Return the first processed booking (subsequent ones are processed but not returned)
    }

    @Override
    @CacheEvict(value = "bookings", key = "#statusUpdateDTO.bookingId")
    public Mono<DeliveryBooking> updateBookingStatus(DeliveryStatusUpdateDTO statusUpdateDTO) {
        return bookingRepository.findById(statusUpdateDTO.getBookingId())
                .switchIfEmpty(Mono.error(new RuntimeException(" Booking not found")))
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
                    return Mono.fromFuture(kafkaProducerService.sendStatusUpdate(event))
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(updatedBooking);
                })
                .doOnSuccess(updatedBooking -> log.info("Booking status updated and Kafka event sent: {}", updatedBooking))
                .doOnError(error -> log.error(" Error updating booking status", error));
    }

    // Optional shutdown hook if any other resources need cleanup
    @PreDestroy
    public void shutdown() {
        // No executor to shut down currently
    }
}
