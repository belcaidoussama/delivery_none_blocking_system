package com.system.delivery.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.system.delivery.entity.DeliverySlot;




public interface DeliverySlotRepository extends R2dbcRepository<DeliverySlot, Long> {
}