package com.gdc.requests_management.kafka;

import com.gdc.requests_management.service.EmailService;
import com.gdc.requests_management.service.UserDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailService emailService;
    private final UserDriverService userDriverService;

    public void sendToTopic(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendToUser(UUID userId, String message) {
        kafkaTemplate.send("user-notifications", userId.toString(), message);

        // Send email to user
        String userEmail = userDriverService.getEmailByUserId(userId);
        if (userEmail != null) {
            emailService.sendEmail(userEmail, "User Notification", message);
        }
    }

    public void sendToDriver(UUID driverId, String message) {
        kafkaTemplate.send("driver-notifications", driverId != null ? driverId.toString() : "all", message);

        // Send email to driver
        if (driverId != null) {
            String driverEmail = userDriverService.getEmailByUserId(driverId);
            if (driverEmail != null) {
                emailService.sendEmail(driverEmail, "Driver Notification", message);
            }
        }
    }

    public void sendToAllDrivers(String message) {
        kafkaTemplate.send("driver-notifications", "all", message);

        // Note: Broadcasting email to all drivers requires fetching all driver emails
        // Consider implementing logic to fetch all driver emails if needed
    }
}