package com.system.delivery.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryModeType {
    DRIVE("DRIVE"),
    DELIVERY("DELIVERY"),
    DELIVERY_TODAY("DELIVERY_TODAY"),
    DELIVERY_ASAP("DELIVERY_ASAP");

    private final String value;

    DeliveryModeType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DeliveryModeType fromString(String key) {
        for (DeliveryModeType type : DeliveryModeType.values()) {
            if (type.value.equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid delivery mode type: " + key);
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}