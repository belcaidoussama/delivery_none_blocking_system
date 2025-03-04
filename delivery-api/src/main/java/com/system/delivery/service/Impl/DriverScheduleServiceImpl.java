package com.system.delivery.service.Impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.DriverScheduleDTO;
import com.system.delivery.entity.DriverSchedule;
import com.system.delivery.repository.DriverScheduleRepository;
import com.system.delivery.service.DriverScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DriverScheduleServiceImpl implements DriverScheduleService{

    private final DriverScheduleRepository driverScheduleRepository;
    
    @CacheEvict(value = "driverSchedules", allEntries = true)
    public Mono<DriverSchedule> createDriverSchedule(DriverScheduleDTO deliveryslot) {
        log.info(" Creating new deliveryslot: {}", deliveryslot);
        return driverScheduleRepository.save(deliveryslot.toEntity())
                .doOnSuccess(savedSlot -> log.info("Slot successfully created: {}", savedSlot))
                .doOnError(error -> log.error(" Error creating deliveryslot", error));
    }

    @Override
    @Cacheable(value = "driverSchedules", key = "#id")
    public Mono<DriverSchedule> getDriverScheduleById(Long id) {
        log.info(" Fetching deliveryslot with ID: {}", id);
        return driverScheduleRepository.findById(id)
                .doOnSuccess(deliveryslot -> log.info(" Slot found: {}", deliveryslot))
                .doOnError(error -> log.error("Error fetching deliveryslot", error));
    }

    public Mono<Void> deleteDriverSchedule(Long id) {
        log.info(" Deleting deliveryslot with ID: {}", id);
        return driverScheduleRepository.deleteById(id)
                .doOnSuccess(unused -> log.info(" Slot deleted successfully"))
                .doOnError(error -> log.error(" Error deleting deliveryslot", error));
    }

    @Override
    @Cacheable(value = "driverSchedules")
    public Flux<DriverSchedule> getAllDriverSchedules() {
        return driverScheduleRepository.findAll();
    }
    
}
