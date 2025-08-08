package com.gdc.requests_management.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class LocationDTO implements Serializable {
    private double latitude;
    private double longitude;
    private String timestamp; // ISO 8601 format
}