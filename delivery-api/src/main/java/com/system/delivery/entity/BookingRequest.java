package com.system.delivery.entity;

import java.io.Serializable;
import java.time.LocalDate;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest implements Serializable {
    private Long clientId;
    private Long slotId;
    private LocalDate deliveryDate;
}
