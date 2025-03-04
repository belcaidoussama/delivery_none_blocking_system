package com.system.delivery.repository;


import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.system.delivery.entity.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Mono<User> findByUsername(String username);
}
