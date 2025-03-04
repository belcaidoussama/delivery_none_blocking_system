package com.system.delivery.service;

import com.system.delivery.dto.DeliveryBookingDTO;
import com.system.delivery.dto.DeliveryStatusUpdateDTO;
import com.system.delivery.entity.DeliveryBooking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeliveryBookingService {
    public Mono<DeliveryBooking> createDeliveryBooking(DeliveryBookingDTO deliveryBooking) ;
    public Flux<DeliveryBooking> getAllDeliveryBookings();
    public Mono<DeliveryBooking> getDeliveryBookingById(Long id) ;
    public Mono<DeliveryBooking> updateBookingStatus(DeliveryStatusUpdateDTO statusUpdateDTO) ;
}
