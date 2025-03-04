package com.system.delivery.entity;


import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("delivery_driver")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryDriver extends User {
    private String vehicleType;

    public DeliveryDriver(String email, String username, String password, String vehicleType) {
        super(null, email, username, password, Role.DELIVERY_DRIVER);
        this.vehicleType = vehicleType;
    }
}
