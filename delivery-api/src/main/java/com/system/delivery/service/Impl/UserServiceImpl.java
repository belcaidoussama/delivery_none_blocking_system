package com.system.delivery.service.Impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.ClientDTO;
import com.system.delivery.dto.DeliveryDriverDTO;
import com.system.delivery.dto.UserDTO;
import com.system.delivery.dto.UserMapper;
import com.system.delivery.entity.User;
import com.system.delivery.repository.UserRepository;
import com.system.delivery.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(UserDTO user) {
        if (user instanceof ClientDTO clientDTO) {
            return userRepository.save(UserMapper.toClient(clientDTO));
        } else if (user instanceof DeliveryDriverDTO driverDTO) {
            return userRepository.save(UserMapper.toDeliveryDriver(driverDTO));
        } else {
            return Mono.error(new IllegalArgumentException("Invalid user type"));
        }
    }


    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userEntity -> org.springframework.security.core.userdetails.User.builder() // Specify Security's User
                        .username(userEntity.getUsername())  // Convert entity to UserDetails
                        .password(userEntity.getPassword())  // Ensure password is encoded
                        .roles(userEntity.getRole().name())         // Set roles
                        .build()
                )
                .cast(UserDetails.class); // Ensure it returns Mono<UserDetails>
    }
}