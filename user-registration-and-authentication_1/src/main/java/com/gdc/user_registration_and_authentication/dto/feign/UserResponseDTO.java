package com.gdc.user_registration_and_authentication.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String displayName;
    private String email;
    private String phoneNumber;

}
