package com.gdc.user_registration_and_authentication.dto.request;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String emailOrPhone;
    private String password;
}

