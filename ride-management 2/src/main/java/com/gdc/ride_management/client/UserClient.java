package com.gdc.ride_management.client;

import com.gdc.ride_management.dto.response.StandardResponse;
import com.gdc.ride_management.dto.response.UserResponseDTO;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        configuration = com.gdc.ride_management.config.InternalFeignConfig.class)

public interface UserClient {
    @GetMapping("/api/users/{userId}")
    StandardResponse<UserResponseDTO> getUserDetails(@PathVariable("userId") UUID userId);
}
