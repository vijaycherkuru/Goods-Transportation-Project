package com.gdc.requests_management.service;

import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.response.StandardResponse;
import com.gdc.requests_management.feign.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDriverService {

    private final UserServiceClient userServiceClient;
    private final Logger logger = LoggerFactory.getLogger(UserDriverService.class);

    public String getEmailByUserId(UUID userId) {
        try {
            StandardResponse<UserResponseDTO> response = userServiceClient.getUserDetails(userId);
            UserResponseDTO user = response.getData();

            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                logger.warn("⚠️ Invalid user or missing email for user ID: {}", userId);
                return null;
            }

            logger.info("✅ Fetched email {} for user ID {}", user.getEmail(), userId);
            return user.getEmail();
        } catch (Exception e) {
            logger.error("❌ Error fetching email for user ID {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}
