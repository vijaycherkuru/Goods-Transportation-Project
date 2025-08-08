package com.gdc.user_registration_and_authentication.dto.response;



import com.gdc.user_registration_and_authentication.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponseDTO {
    private UUID id;
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

