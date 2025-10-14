package com.aurionpro.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentRequestType {
    VENDOR,
    PAYROLL;

    @JsonCreator
    public static PaymentRequestType fromString(String value) {
        if (value == null) {
            return null;
        }
        // Convert to uppercase and trim
        String normalized = value.trim().toUpperCase();
        for (PaymentRequestType type : PaymentRequestType.values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid request type: " + value +
                ". Allowed values: VENDOR or PAYROLL");
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

