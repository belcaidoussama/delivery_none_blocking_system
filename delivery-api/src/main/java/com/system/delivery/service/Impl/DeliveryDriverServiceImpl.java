package com.system.delivery.service.Impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.system.delivery.dto.DeliveryDriverDTO;
import com.system.delivery.dto.UserMapper;
import com.system.delivery.entity.DeliveryDriver;
import com.system.delivery.repository.DeliveryDriverRepository;
import com.system.delivery.service.DeliveryDriverService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DeliveryDriverServiceImpl implements DeliveryDriverService{
    private final DeliveryDriverRepository driverRepository;

    @Override
    @CacheEvict(value = "drivers", allEntries = true)
    public Mono<DeliveryDriver> createDeliveryDriver(DeliveryDriverDTO driver) {
        return driverRepository.save(UserMapper.toDeliveryDriver(driver));
    }
    @Override
    @Cacheable(value = "drivers")
    public Flux<DeliveryDriver> getAllDeliveryDrivers() {
        return driverRepository.findAll();
    }
    @Override
    @Cacheable(value = "drivers", key = "#id")
    public Mono<DeliveryDriver> findById(Long id ) {
        return driverRepository.findById(id);
    }

}
