package com.gdc.user_registration_and_authentication.dto.request;

import com.gdc.user_registration_and_authentication.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class UpdateProfileRequestDTO {
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private Gender gender;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}


