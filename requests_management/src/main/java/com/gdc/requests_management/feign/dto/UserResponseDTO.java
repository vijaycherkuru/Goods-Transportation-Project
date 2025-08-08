package com.gdc.requests_management.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String displayName;
    private String email;
    private String phoneNumber;

}

