package com.system.delivery.service;

    import com.system.delivery.dto.DeliveryDriverDTO;
import com.system.delivery.entity.DeliveryDriver;

import reactor.core.publisher.Flux;import reactor.core.publisher.Mono;

public interface DeliveryDriverService {
    
    public Mono<DeliveryDriver> createDeliveryDriver(DeliveryDriverDTO driver) ;
    public Flux<DeliveryDriver> getAllDeliveryDrivers();
    public Mono<DeliveryDriver> findById(Long id) ;
}
