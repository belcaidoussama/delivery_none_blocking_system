package com.system.delivery.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.system.delivery.dto.DeliverySlotDTO;
import com.system.delivery.entity.DeliverySlot;
import com.system.delivery.service.DeliverySlotService;
import com.system.delivery.utils.HateoasUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/deliverySlots")
public class DeliverySlotController {

    private final DeliverySlotService deliverySlotService;

    //  Get DeliverySlot By ID with Proper Error Handling
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EntityModel<DeliverySlot>>> getDeliverySlotById(@PathVariable Long id) {
        return deliverySlotService.getDeliverySlotById(id)
                .flatMap(deliverySlot -> HateoasUtils.createEntityModel(
                        deliverySlot,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliverySlotController.class)
                                .getDeliverySlotById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Delivery Slot not found"))); //  Triggers ControllerAdvice
    }

    //  Get All DeliverySlots with Proper Error Handling
    @GetMapping
    public Mono<CollectionModel<EntityModel<DeliverySlot>>> getAllDeliverySlots() {
        return deliverySlotService.getAllDeliverySlots()
                .flatMap(deliverySlot -> HateoasUtils.createEntityModel(
                        deliverySlot,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliverySlotController.class)
                                .getDeliverySlotById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .collectList()
                .flatMap(deliverySlotModels -> WebFluxLinkBuilder.linkTo(
                        WebFluxLinkBuilder.methodOn(DeliverySlotController.class).getAllDeliverySlots())
                        .withSelfRel()
                        .toMono()
                        .map(selfLink -> CollectionModel.of(deliverySlotModels, selfLink))
                ); 
    }

    //  Create DeliverySlot with Validation & Proper Error Handling
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<EntityModel<DeliverySlot>>> createDeliverySlot(@Valid @RequestBody DeliverySlotDTO deliverySlotDTO) {
        return deliverySlotService.createDeliverySlot(deliverySlotDTO)
                .flatMap(savedDeliverySlot -> HateoasUtils.createEntityModel(
                        savedDeliverySlot,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliverySlotController.class)
                                .getDeliverySlotById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(deliverySlotEntity -> ResponseEntity.status(HttpStatus.CREATED).body(deliverySlotEntity))
                .onErrorResume(WebExchangeBindException.class, ex -> 
                    Mono.error(new IllegalArgumentException("Validation failed: " + ex.getMessage()))
                );
    }
}
