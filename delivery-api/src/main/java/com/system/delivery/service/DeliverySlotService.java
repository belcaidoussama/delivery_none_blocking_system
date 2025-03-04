package com.system.delivery.service;

import com.system.delivery.dto.DeliverySlotDTO;
import com.system.delivery.entity.DeliverySlot;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeliverySlotService {
    public Mono<DeliverySlot> createDeliverySlot(DeliverySlotDTO deliverySlot) ;
    public Flux<DeliverySlot> getAllDeliverySlots();
    public Mono<DeliverySlot> getDeliverySlotById(Long id) ;
}
