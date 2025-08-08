package com.gdc.ride_management.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VehicleType {
    CAR,
    BIKE,
    VAN,
    AUTO;

    @JsonCreator
    public static VehicleType fromString(String value) {
        return switch (value.toLowerCase().replace(" ", "_")) {
            case "CAR" -> CAR;
            case "VAN" -> VAN;
            case "BIKE" -> BIKE;
            case "AUTO" -> AUTO;
            default -> throw new IllegalArgumentException("Invalid vehicleType: " + value);
        };
    }
}