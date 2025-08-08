package com.gdc.requests_management.controller;

import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import com.gdc.requests_management.dto.request.PaymentNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WebSocketNotificationHandler notificationHandler;

    // DTO for generic notification requests
    public static class NotificationRequest {
        public UUID userId;
        public String message;
        public String title;
        public String path;
    }

    // DTO for tracking update notifications
    public static class TrackingNotificationRequest {
        public UUID senderUserId;
        public UUID rideUserId;
        public String requestId;
        public String status;
        public String location;
    }

    // Send a simple notification to a user (WebSocket + Email fallback)
    @PostMapping("/user")
    public ResponseEntity<Void> notifyUser(@RequestBody NotificationRequest req) {
        notificationHandler.sendUserNotification(req.userId, req.message);
        return ResponseEntity.ok().build();
    }

    // Send a notification to a user with a custom path and title
    @PostMapping("/user/custom")
    public ResponseEntity<Void> notifyUserCustom(@RequestBody NotificationRequest req) {
        notificationHandler.sendNotificationToUser(req.userId, req.title, req.message, req.path);
        return ResponseEntity.ok().build();
    }

    // Send a real-time update to a user
    @PostMapping("/user/update")
    public ResponseEntity<Void> notifyUserUpdate(@RequestBody NotificationRequest req) {
        notificationHandler.sendRealTimeUpdate(req.userId, req.message);
        return ResponseEntity.ok().build();
    }

    // Broadcast to all drivers
    @PostMapping("/drivers/broadcast")
    public ResponseEntity<Void> broadcastToDrivers(@RequestBody NotificationRequest req) {
        notificationHandler.broadcastToAllDrivers(req.message);
        return ResponseEntity.ok().build();
    }

    // Send a driver-specific notification
    @PostMapping("/driver")
    public ResponseEntity<Void> notifyDriver(@RequestBody NotificationRequest req) {
        notificationHandler.sendDriverNotification(req.userId, req.message);
        return ResponseEntity.ok().build();
    }

    // Send a tracking update (to sender and driver)
    @PostMapping("/tracking")
    public ResponseEntity<Void> sendTrackingUpdate(@RequestBody TrackingNotificationRequest req) {
        notificationHandler.sendTrackingUpdate(
                req.senderUserId,
                req.rideUserId,
                req.requestId,
                req.status,
                req.location
        );
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> sendPaymentNotification(@RequestBody PaymentNotificationRequest req) {
        notificationHandler.sendPaymentNotification(
                req.senderUserId,
                req.rideUserId,
                req.requestId,
                req.paymentStatus,
                req.amount
        );
        return ResponseEntity.ok().build();
    }
}
