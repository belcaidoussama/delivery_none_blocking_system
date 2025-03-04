package com.system.delivery.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("driver_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverSchedule implements Serializable {
    @Id
    private Long id;
    private Long deliveryDriverId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
