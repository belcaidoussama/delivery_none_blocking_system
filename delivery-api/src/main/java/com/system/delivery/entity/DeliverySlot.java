package com.system.delivery.entity;


import java.io.Serializable;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("delivery_slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySlot implements Serializable{
    @Id
    private Long id;
    private DeliveryModeType deliveryModeType;
    private LocalTime startTime;
    private LocalTime endTime;
}