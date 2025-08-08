package com.gdc.requests_management.dto.request;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String emailOrPhone;
    private String password;
}