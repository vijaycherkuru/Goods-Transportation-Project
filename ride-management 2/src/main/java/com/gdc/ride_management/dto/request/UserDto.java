package com.gdc.ride_management.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String displayName;
    @JsonProperty("phoneNumber")
    private String phone;
    private String email;
    private String name;

}
