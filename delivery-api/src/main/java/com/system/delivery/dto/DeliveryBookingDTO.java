package com.system.delivery.dto;

import java.time.LocalDate;

import com.system.delivery.entity.DeliveryBooking;
import com.system.delivery.entity.DeliveryStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryBookingDTO {
    @NotNull
    private Long slotId;
    @NotNull
    private Long clientId;

    @NotNull
    private LocalDate deliveryDate;

    public DeliveryBooking toEntity() {
        return new DeliveryBooking(null, this.slotId, this.clientId, null,deliveryDate, DeliveryStatus.PENDING);
    }
}
