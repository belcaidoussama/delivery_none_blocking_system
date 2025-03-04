package com.system.delivery.entity;

public enum Role {
    CLIENT("CLIENT"),
    DELIVERY_DRIVER("DELIVERY_DRIVER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}