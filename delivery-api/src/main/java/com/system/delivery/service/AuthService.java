package com.system.delivery.service;


import com.system.delivery.config.security.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Mono<String> authenticate(String username, String password) {
        return userService.findByUsername(username)
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.just(jwtUtil.generateToken(user.getUsername()));
                    } else {
                        return Mono.error(new IllegalArgumentException("Invalid credentials"));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }
}
