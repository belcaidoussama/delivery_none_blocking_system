package com.system.delivery.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.system.delivery.dto.UserDTO;
import com.system.delivery.entity.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    public Mono<User> createUser(UserDTO client) ;
    public Flux<User> getAllUsers();
    public Mono<User> findById(Long id) ;
    Mono<UserDetails> findByUsername(String username);
    //Mono<User> findByUsername(String username);


}
