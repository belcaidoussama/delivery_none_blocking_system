package com.system.delivery.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.system.delivery.dto.DriverScheduleDTO;
import com.system.delivery.entity.DriverSchedule;
import com.system.delivery.service.DriverScheduleService;
import com.system.delivery.utils.HateoasUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/driverSchedules")
public class DriverScheduleController {
    private final DriverScheduleService driverScheduleService;
    //   Get DriverSchedule By ID with Proper Error Handling
    @GetMapping("/{id}")
    public Mono<ResponseEntity<EntityModel<DriverSchedule>>> getDriverScheduleById(@PathVariable Long id) {
        return driverScheduleService.getDriverScheduleById(id)
                .flatMap(driverSchedule -> HateoasUtils.createEntityModel(
                        driverSchedule,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DriverScheduleController.class)
                                .getDriverScheduleById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Driver Schedule not found"))); //   Triggers ControllerAdvice
    }

    //   Get All DriverSchedules with Proper Error Handling
    @GetMapping
    public Mono<CollectionModel<EntityModel<DriverSchedule>>> getAllDriverSchedules() {
        return driverScheduleService.getAllDriverSchedules()
                .flatMap(driverSchedule -> HateoasUtils.createEntityModel(
                        driverSchedule,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DriverScheduleController.class)
                                .getDriverScheduleById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .collectList()
                .flatMap(driverScheduleModels -> WebFluxLinkBuilder.linkTo(
                        WebFluxLinkBuilder.methodOn(DriverScheduleController.class).getAllDriverSchedules())
                        .withSelfRel()
                        .toMono()
                        .map(selfLink -> CollectionModel.of(driverScheduleModels, selfLink))
                )
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No driver schedules found"))); //   Triggers ControllerAdvice
    }

    //   Create DriverSchedule with Validation & Proper Error Handling
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<EntityModel<DriverSchedule>>> createDriverSchedule(@Valid @RequestBody DriverScheduleDTO driverScheduleDTO) {
        return driverScheduleService.createDriverSchedule(driverScheduleDTO)
                .flatMap(savedDriverSchedule -> HateoasUtils.createEntityModel(
                        savedDriverSchedule,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(DriverScheduleController.class)
                                .getDriverScheduleById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(driverScheduleEntity -> ResponseEntity.status(HttpStatus.CREATED).body(driverScheduleEntity))
                .onErrorResume(WebExchangeBindException.class, ex -> 
                    Mono.error(new IllegalArgumentException("Validation failed: " + ex.getMessage()))
                );
    }
}
