package com.gdc.requests_management.client;

import com.gdc.requests_management.dto.request.LoginRequestDTO;
import com.gdc.requests_management.dto.response.StandardResponse;
import com.gdc.requests_management.feign.dto.LoginResponseDTO;
import com.gdc.requests_management.feign.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        configuration = com.gdc.requests_management.config.InternalFeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/api/auth/login")
    LoginResponseDTO login(@RequestBody LoginRequestDTO request);

    @GetMapping("/api/users/{userId}")
    StandardResponse<UserResponseDTO> getUserDetails(@PathVariable("userId") UUID userId);

    @PutMapping("/users/{userId}/ban")
    void banUser(@PathVariable("userId") UUID userId, @RequestParam("reason") String reason);
}