package com.system.delivery.dto;

import com.system.delivery.entity.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class UserDTO {
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
    

    @NotBlank
    private String email;


    private Role role;
}