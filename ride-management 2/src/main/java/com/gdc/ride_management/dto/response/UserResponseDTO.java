package com.gdc.ride_management.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String displayName;
    private String email;
    @JsonProperty("phoneNumber")
    private String phone;
}
