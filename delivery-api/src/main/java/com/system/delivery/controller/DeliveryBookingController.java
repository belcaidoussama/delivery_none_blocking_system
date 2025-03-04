package com.system.delivery.controller;

import java.util.Map;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.system.delivery.dto.DeliveryBookingDTO;
import com.system.delivery.dto.DeliveryStatusUpdateDTO;
import com.system.delivery.entity.DeliveryBooking;
import com.system.delivery.service.DeliveryBookingService;
import com.system.delivery.utils.HateoasUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/deliveryBookings")
public class DeliveryBookingController {
    
        private final DeliveryBookingService deliveryBookingService;

    // Get DeliveryBooking By ID with Proper Error Handling
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EntityModel<DeliveryBooking>>> getDeliveryBookingById(@PathVariable Long id) {
        return deliveryBookingService.getDeliveryBookingById(id)
                .flatMap(deliveryBooking -> HateoasUtils.createEntityModel(
                        deliveryBooking,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryBookingController.class)
                                .getDeliveryBookingById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Delivery Booking not found"))); //  Triggers ControllerAdvice
    }

    //  Get All DeliveryBookings with Proper Error Handling
    @GetMapping
    public Mono<CollectionModel<EntityModel<DeliveryBooking>>> getAllDeliveryBookings() {
        return deliveryBookingService.getAllDeliveryBookings()
                .flatMap(deliveryBooking -> HateoasUtils.createEntityModel(
                        deliveryBooking,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryBookingController.class)
                                .getDeliveryBookingById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .collectList()
                .flatMap(deliveryBookingModels -> WebFluxLinkBuilder.linkTo(
                        WebFluxLinkBuilder.methodOn(DeliveryBookingController.class).getAllDeliveryBookings())
                        .withSelfRel()
                        .toMono()
                        .map(selfLink -> CollectionModel.of(deliveryBookingModels, selfLink))
                ); //  Triggers ControllerAdvice
    }

    //  Create DeliveryBooking with Validation & Proper Error Handling
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<EntityModel<DeliveryBooking>>> createDeliveryBooking(@Valid @RequestBody DeliveryBookingDTO deliveryBookingDTO) {
        return deliveryBookingService.createDeliveryBooking(deliveryBookingDTO)
                .flatMap(savedDeliveryBooking -> HateoasUtils.createEntityModel(
                        savedDeliveryBooking,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DeliveryBookingController.class)
                                .getDeliveryBookingById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(deliveryBookingEntity -> ResponseEntity.status(HttpStatus.CREATED).body(deliveryBookingEntity))
                .onErrorResume(WebExchangeBindException.class, ex -> 
                    Mono.error(new IllegalArgumentException("Validation failed: " + ex.getMessage()))
                );
    }


    @PutMapping("/status")
        public Mono<ResponseEntity<Void>> updateBookingStatus(@Valid @RequestBody DeliveryStatusUpdateDTO statusUpdateDTO) {
        return deliveryBookingService.updateBookingStatus(statusUpdateDTO)
                .then(Mono.just(ResponseEntity.ok().build()));
        }
}
