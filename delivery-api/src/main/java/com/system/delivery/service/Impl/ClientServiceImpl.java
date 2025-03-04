package com.system.delivery.service.Impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.ClientDTO;
import com.system.delivery.dto.UserMapper;
import com.system.delivery.entity.Client;
import com.system.delivery.repository.ClientRepository;
import com.system.delivery.service.ClientService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService{
    private final ClientRepository clientRepository;

    @Override
    @CacheEvict(value = "clients", allEntries = true)
    public Mono<Client> createClient(ClientDTO client) {
        return clientRepository.save(UserMapper.toClient(client));
    }
    @Override
    @Cacheable(value = "clients")
    public Flux<Client> getAllClients() {
        return clientRepository.findAll();
    }
    @Override
    @Cacheable(value = "clients", key = "#id")
    public Mono<Client> findById(Long id ) {
        return clientRepository.findById(id);
    }
    @Override
    public Mono<Client> findByUsername(String username) {
        return clientRepository.findByUsername();
    }
    
}
