package com.system.delivery.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class DeliveryDriverDTO extends UserDTO {

    @NotBlank
    private String vehicleType;  
}