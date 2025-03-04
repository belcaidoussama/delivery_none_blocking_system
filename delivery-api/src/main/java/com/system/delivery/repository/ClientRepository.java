package com.system.delivery.repository;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.system.delivery.entity.Client;

import reactor.core.publisher.Mono;



public interface ClientRepository extends R2dbcRepository<Client, Long> {
    @Query("SELECT * FROM users WHERE username = :username")
    public Mono<Client> findByUsername();
}