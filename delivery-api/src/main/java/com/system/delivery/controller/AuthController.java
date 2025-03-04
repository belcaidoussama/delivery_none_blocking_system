package com.system.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.system.delivery.config.security.JwtUtil;
import com.system.delivery.dto.ClientDTO;
import com.system.delivery.dto.DeliveryDriverDTO;
import com.system.delivery.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService; //  Used for user management
    private final PasswordEncoder passwordEncoder;

    //  Login Endpoint (Generates JWT)
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        return userService.findByUsername(authRequest.username()) //  Uses getByUsername() to find user
                .flatMap(user -> {
                    if (passwordEncoder.matches(authRequest.password(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getUsername());
                        return Mono.just(ResponseEntity.ok(new AuthResponse(token))); //  Return JWT token
                    } else {
                        return Mono.just(ResponseEntity.status(401).body(new AuthResponse("Invalid credentials")));
                    }
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(404).body(new AuthResponse("User not found"))));
    }

    //  Register Client Endpoint
    @PostMapping("/register/client")
    public Mono<ResponseEntity<String>> registerClient(@Valid @RequestBody ClientDTO client) {
        client.setPassword(passwordEncoder.encode(client.getPassword())); //  Hash password before saving

        return userService.findByUsername(client.getUsername())
                .flatMap(existingUser ->
                 Mono.just(ResponseEntity.badRequest().body("Username already exists!"))
                 )
                .switchIfEmpty(
                    userService.createUser(client)
                        .thenReturn(ResponseEntity.ok("Client registered successfully!")));
    }

    //  Register Driver Endpoint
    @PostMapping("/register/driver")
    public Mono<ResponseEntity<String>> registerDriver(@Valid @RequestBody DeliveryDriverDTO driver) {
        driver.setPassword(passwordEncoder.encode(driver.getPassword())); //  Hash password before saving

        return userService.findByUsername(driver.getUsername())
                .flatMap(existingUser -> Mono.just(ResponseEntity.badRequest().body("Username already exists!")))
                .switchIfEmpty(userService.createUser(driver)
                        .thenReturn(ResponseEntity.ok("Driver registered successfully!")));
    }
}

//  Request & Response DTOs
record AuthRequest(String username, String password) {}
record AuthResponse(String token) {}
