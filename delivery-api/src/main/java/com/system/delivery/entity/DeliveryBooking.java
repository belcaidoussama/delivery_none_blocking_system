package com.system.delivery.entity;


import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.kafka.shaded.com.google.protobuf.DescriptorProtos;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("delivery_booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryBooking implements Serializable {
    @Id
    private Long id;

    @NotNull
    private Long slotId;

    @NotNull
    private Long clientId;

    private Long deliveryDriverId;

    @NotNull
    private LocalDate deliveryDate;

    private DeliveryStatus status;
}
