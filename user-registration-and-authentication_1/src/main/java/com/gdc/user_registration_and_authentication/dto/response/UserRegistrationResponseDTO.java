package com.gdc.user_registration_and_authentication.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationResponseDTO {
    private UUID userId;
    private String message;
    private String email;
    private String phone;


}

