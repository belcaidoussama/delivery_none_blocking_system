package com.system.delivery.repository;




import com.system.delivery.entity.DeliveryBooking;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DeliveryBookingRepository extends R2dbcRepository<DeliveryBooking, Long> {}
