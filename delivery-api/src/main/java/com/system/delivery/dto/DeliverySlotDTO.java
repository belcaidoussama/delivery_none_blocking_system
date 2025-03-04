package com.system.delivery.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.system.delivery.entity.DeliveryModeType;
import com.system.delivery.entity.DeliverySlot;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DeliverySlotDTO {
    //@NotNull
    @JsonProperty("deliveryModeType")
    private DeliveryModeType deliveryModeType;
    //@NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;
   //@NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;

    public DeliverySlot toEntity() {
        return new DeliverySlot(null, this.deliveryModeType, this.startTime, this.endTime);
    }
}
