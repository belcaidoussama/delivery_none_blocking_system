package com.system.delivery.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.system.delivery.dto.DeliveryDriverDTO;
import com.system.delivery.entity.DeliveryDriver;
import com.system.delivery.service.DeliveryDriverService;
import com.system.delivery.utils.HateoasUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/drivers")
public class DeliveryDriverController {
    
        private final DeliveryDriverService deliveryDriverService;

        //  Get DeliveryDriver By ID with Proper Error Handling
        @GetMapping("/{id}")
        public Mono<ResponseEntity<EntityModel<DeliveryDriver>>> getDeliveryDriverById(@PathVariable Long id) {
        return deliveryDriverService.findById(id)
                .flatMap(driver -> HateoasUtils.createEntityModel(
                        driver,
                        d -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryDriverController.class)
                                .getDeliveryDriverById(d.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Delivery Driver not found"))); // Triggers ControllerAdvice
        }

        //  Get All DeliveryDrivers with Proper Error Handling
        @GetMapping
        public Mono<CollectionModel<EntityModel<DeliveryDriver>>> getAllDeliveryDrivers() {
        return deliveryDriverService.getAllDeliveryDrivers()
                .flatMap(driver -> HateoasUtils.createEntityModel(
                        driver,
                        d -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryDriverController.class)
                                .getDeliveryDriverById(d.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .collectList()
                .flatMap(driverModels -> WebFluxLinkBuilder.linkTo(
                        WebFluxLinkBuilder.methodOn(DeliveryDriverController.class).getAllDeliveryDrivers())
                        .withSelfRel()
                        .toMono()
                        .map(selfLink -> CollectionModel.of(driverModels, selfLink))
                ); 
        }

        //  Create DeliveryDriver with Validation & Proper Error Handling
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Mono<ResponseEntity<EntityModel<DeliveryDriver>>> createDeliveryDriver(@Valid @RequestBody DeliveryDriverDTO deliveryDriverDTO) {
        return deliveryDriverService.createDeliveryDriver(deliveryDriverDTO)
                .flatMap(savedDriver -> HateoasUtils.createEntityModel(
                        savedDriver,
                        d -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryDriverController.class)
                                .getDeliveryDriverById(d.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(driverEntity -> ResponseEntity.status(HttpStatus.CREATED).body(driverEntity))
                .onErrorResume(WebExchangeBindException.class, ex -> 
                        Mono.error(new IllegalArgumentException("Validation failed: " + ex.getMessage()))
                );
        }
}