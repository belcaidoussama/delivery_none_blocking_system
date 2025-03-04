package com.system.delivery.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.system.delivery.entity.DriverSchedule;

public class DriverScheduleDTO {
   // @NotNull
    @JsonProperty("deliveryDriverId")
    private Long deliveryDriverId;
    //@NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    //@NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;
   //// @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;
  

    public DriverSchedule toEntity() {
        return new DriverSchedule(null, this.deliveryDriverId, this.scheduleDate, this.startTime, this.endTime, Boolean.TRUE);
    }
}
