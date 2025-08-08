package com.gdc.requests_management.websocket;

import com.gdc.requests_management.service.EmailService;
import com.gdc.requests_management.service.UserDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final UserDriverService userDriverService;

    /**
     * Sends a generic notification to a user via WebSocket and fallback email.
     */
    public void sendUserNotification(UUID userId, String message) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);

        String userEmail = userDriverService.getEmailByUserId(userId);
        if (userEmail != null) {
            emailService.sendEmail(userEmail, "User Notification", message);
        }
    }

    /**
     * Sends a notification to a specific WebSocket path and emails the user.
     */
    public void sendNotificationToUser(UUID userId, String title, String message, String path) {
        messagingTemplate.convertAndSendToUser(userId.toString(), path, message);

        String email = userDriverService.getEmailByUserId(userId);
        if (email != null) {
            emailService.sendEmail(email, title, message);
        }
    }

    /**
     * Sends real-time WebSocket updates to user at a specific updates queue.
     */
    public void sendRealTimeUpdate(UUID userId, String message) {
        sendNotificationToUser(userId, "Real-Time Update", message, "/queue/updates");
    }

    /**
     * Broadcasts notifications to all subscribed drivers.
     */
    public void broadcastToAllDrivers(String message) {
        messagingTemplate.convertAndSend("/topic/driver-notifications", message);
        // Future enhancement: also send bulk email to all driver emails
    }


    public void sendDriverNotification(UUID rideUserId, String message) {
        sendNotificationToUser(rideUserId, "Driver Notification", message, "/queue/driver");
    }


    /**
     * Sends location-based tracking updates to user and driver.
     */
    public void sendTrackingUpdate(UUID senderUserId, UUID rideUserId, String requestId, String status, String location) {
        String trackingMessage = String.format("Request %s: Status updated to %s at %s", requestId, status, location);

        if (senderUserId != null) {
            sendNotificationToUser(senderUserId, "Tracking Update", trackingMessage, "/queue/tracking");
        }

        if (rideUserId != null) {
            sendNotificationToUser(rideUserId, "Tracking Update", trackingMessage, "/queue/tracking");
        }
    }

    public void sendPaymentNotification(UUID senderUserId, UUID rideUserId, String requestId, String paymentStatus, String amount) {
        String paymentMessage = String.format("Request %s: Payment %s. Amount: %s", requestId, paymentStatus, amount);

        if (senderUserId != null) {
            sendNotificationToUser(senderUserId, "Payment Notification", paymentMessage, "/queue/payments");
        }

        if (rideUserId != null) {
            sendNotificationToUser(rideUserId, "Payment Notification", paymentMessage, "/queue/payments");
        }
    }

}
