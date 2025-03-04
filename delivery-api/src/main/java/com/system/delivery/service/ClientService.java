package com.system.delivery.service;


import com.system.delivery.dto.ClientDTO;
import com.system.delivery.entity.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ClientService {
    public Mono<Client> createClient(ClientDTO client) ;
    public Flux<Client> getAllClients();
    public Mono<Client> findById(Long id) ;
    public Mono<Client> findByUsername(String username);
}
