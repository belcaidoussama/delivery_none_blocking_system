package com.system.delivery.dto;

import com.system.delivery.entity.DeliveryStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusUpdateDTO {

    @NotNull
    private Long bookingId;

    @NotNull
    private DeliveryStatus status;
}