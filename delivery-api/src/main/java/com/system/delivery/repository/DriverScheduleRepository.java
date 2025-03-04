package com.system.delivery.repository;


import java.time.LocalDate;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.system.delivery.entity.DriverSchedule;

import reactor.core.publisher.Mono;



public interface DriverScheduleRepository extends R2dbcRepository<DriverSchedule, Long> {

    Mono<DriverSchedule> findByScheduleDateAndAvailable(LocalDate deliveryDate, boolean b);

    @Modifying
    @Query("UPDATE driver_schedule SET available = :available WHERE id = :id")
    Mono<Integer> updateAvailability(Long id, boolean available);
}
