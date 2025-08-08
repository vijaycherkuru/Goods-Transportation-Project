package com.gdc.requests_management.feign.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String message;
    private String email;
    private String userId;
    // Add other fields as needed from user-service response
}