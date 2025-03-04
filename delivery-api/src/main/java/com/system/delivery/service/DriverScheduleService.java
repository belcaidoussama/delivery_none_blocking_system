package com.system.delivery.service;

import com.system.delivery.dto.DriverScheduleDTO;
import com.system.delivery.entity.DriverSchedule;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DriverScheduleService {
    public Mono<DriverSchedule> createDriverSchedule(DriverScheduleDTO deliverySchedul) ;
    public Flux<DriverSchedule> getAllDriverSchedules();
    public Mono<DriverSchedule> getDriverScheduleById(Long id) ;
}
