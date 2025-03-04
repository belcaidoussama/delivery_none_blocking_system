package com.system.delivery.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.system.delivery.dto.ClientDTO;
import com.system.delivery.entity.Client;
import com.system.delivery.service.ClientService;
import com.system.delivery.utils.HateoasUtils;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clients")
public class ClientController {
    
    private final ClientService clientService;


    @GetMapping("/{id}")
    public Mono<ResponseEntity<EntityModel<Client>>> getClientById(@PathVariable Long id) throws IllegalArgumentException{
        return clientService.findById(id)
                .flatMap(client -> HateoasUtils.createEntityModel(
                        client,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(ClientController.class)
                                .getClientById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found")));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<Client>>> getAllClients() {
        return clientService.getAllClients()
                .flatMap(client -> HateoasUtils.createEntityModel(
                        client,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(ClientController.class)
                                .getClientById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .collectList()
                .flatMap(clientModels -> 
                    WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(ClientController.class).getAllClients())
                        .withSelfRel()
                        .toMono()
                        .map(selfLink -> CollectionModel.of(clientModels, selfLink))
                );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<EntityModel<Client>>> createClient(@Valid @RequestBody ClientDTO clientDTO) throws IllegalArgumentException{

        return clientService.createClient(clientDTO)
                .flatMap(savedClient -> HateoasUtils.createEntityModel(
                        savedClient,
                        c -> WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(ClientController.class)
                                .getClientById(c.getId()))
                                .withSelfRel()
                                .toMono()
                ))
                .map(clientEntity -> ResponseEntity.status(HttpStatus.CREATED).body(clientEntity))
                .onErrorResume(WebExchangeBindException.class, ex -> 
                    Mono.error(new IllegalArgumentException("Validation failed: " + ex.getMessage()))
                );
    }
}