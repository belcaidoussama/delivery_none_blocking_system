package com.system.delivery.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.system.delivery.entity.DeliveryDriver;

public interface DeliveryDriverRepository extends R2dbcRepository<DeliveryDriver, Long> {}
