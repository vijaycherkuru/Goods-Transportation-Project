package com.gdc.requests_management.dto.request;

import lombok.Data;

@Data
public class LocationSearchRequestDTO {
    private String from;
    private String to;
    private int radius = 10;
}