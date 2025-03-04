package com.system.delivery.service.Impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.DeliverySlotDTO;
import com.system.delivery.entity.DeliverySlot;
import com.system.delivery.repository.DeliverySlotRepository;
import com.system.delivery.service.DeliverySlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@Service
public class DeliverySlotServiceImpl implements DeliverySlotService {

    private final DeliverySlotRepository deliverySlotRepository;

    @Override
    @CacheEvict(value = "deliverySlots", allEntries = true)
    public Mono<DeliverySlot> createDeliverySlot(DeliverySlotDTO deliveryslot) {
        log.info(" Creating new deliveryslot: {}", deliveryslot);
        return deliverySlotRepository.save(deliveryslot.toEntity())
                .doOnSuccess(savedSlot -> log.info("Slot successfully created: {}", savedSlot))
                .doOnError(error -> log.error(" Error creating deliveryslot", error));
    }
    @Override
    @Cacheable(value = "deliverySlots", key = "#id")
    public Mono<DeliverySlot> getDeliverySlotById(Long id) {
        log.info(" Fetching deliveryslot with ID: {}", id);
        return deliverySlotRepository.findById(id)
                .doOnSuccess(deliveryslot -> log.info(" Slot found: {}", deliveryslot))
                .doOnError(error -> log.error("Error fetching deliveryslot", error));
    }

    public Mono<Void> deleteDeliverySlot(Long id) {
        log.info(" Deleting deliveryslot with ID: {}", id);
        return deliverySlotRepository.deleteById(id)
                .doOnSuccess(unused -> log.info(" Slot deleted successfully"))
                .doOnError(error -> log.error(" Error deleting deliveryslot", error));
    }

    @Override
    @Cacheable(value = "deliverySlots")
    public Flux<DeliverySlot> getAllDeliverySlots() {
        return deliverySlotRepository.findAll();
    }
    
}
