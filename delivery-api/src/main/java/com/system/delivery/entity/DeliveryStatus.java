package com.system.delivery.entity;


public enum DeliveryStatus {
    PENDING("PENDING"),
    IN_TRANSIT("IN_TRANSIT"),
    DELIVERED("DELIVERED");

    private final String value;

    DeliveryStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}