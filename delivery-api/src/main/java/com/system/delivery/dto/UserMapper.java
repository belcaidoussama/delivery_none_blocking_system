package com.system.delivery.dto;


import com.system.delivery.entity.Client;
import com.system.delivery.entity.DeliveryDriver;


public class UserMapper {

    public static Client toClient(ClientDTO dto) {
        return new Client(dto.getEmail(), dto.getUsername(), dto.getPassword(), dto.getAddress());
    }

    public static DeliveryDriver toDeliveryDriver(DeliveryDriverDTO dto) {
        return new DeliveryDriver(dto.getEmail(), dto.getUsername(), dto.getPassword(), dto.getVehicleType());
    }
}