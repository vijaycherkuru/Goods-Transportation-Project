package com.gdc.requests_management.service.impl;

import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.service.EmailService;
import com.gdc.requests_management.service.NotificationService;
import com.gdc.requests_management.service.UserDriverService;
import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final WebSocketNotificationHandler webSocketHandler;
    private final UserDriverService userDriverService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateSecureToken(UUID requestId, UUID rideUserId) {
        return Jwts.builder()
                .setSubject("RequestApproval")
                .claim("requestId", requestId.toString())
                .claim("rideUserId", rideUserId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 mins
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    @Override
    public void handleDriverAndUserNotifications(Request request, RequestDTO dto) {
        UUID driverId = request.getRideUserId(); // ✅ renamed
        String fare = String.format("%.2f", dto.getFare());

        if (driverId != null) {
            String driverMsg = request.getId() + ": New request for your ride from " + dto.getFrom() + " to " + dto.getTo();
            webSocketHandler.sendUserNotification(driverId, driverMsg); // ✅ correct usage

            String token = generateSecureToken(request.getId(), driverId);
            String acceptUrl = "http://localhost:8087/api/v1/requests/" + request.getId() + "/accept?token=" + token;
            String rejectUrl = "http://localhost:8087/api/v1/requests/" + request.getId() + "/reject?token=" + token;

            String driverEmail = userDriverService.getEmailByUserId((driverId)); // ✅ corrected
            if (driverEmail != null) {
                emailService.sendDriverRequestNotification(
                        driverEmail,
                        request.getId().toString(),
                        request.getFrom(),
                        request.getTo(),
                        request.getGoodsDescription(),
                        fare,
                        acceptUrl,
                        rejectUrl
                );
            }

            log.info("✅ Driver notified: {}", driverId);
        }
        else {
            String broadcastMsg = request.getId() + ": New request available from " + dto.getFrom() + " to " + dto.getTo();
            webSocketHandler.broadcastToAllDrivers(broadcastMsg);
        }

        String userMessage = request.getId() + ": Request created successfully";
        webSocketHandler.sendUserNotification(request.getSenderUserId(), userMessage);

        String userEmail = userDriverService.getEmailByUserId(request.getSenderUserId());
        if (userEmail != null) {
            emailService.sendUserConfirmation(
                    userEmail,
                    request.getId().toString(),
                    request.getFrom(),
                    request.getTo(),
                    request.getGoodsDescription()
            );
        }
    }

    @Override
    public void notifyUserAutoRejected(Request request) {
        String email = userDriverService.getEmailByUserId(request.getSenderUserId());
        if (email != null) {
            emailService.sendEmail(
                    email,
                    "❌ Driver Did Not Respond",
                    String.format("Dear User,<br>Your request <b>%s</b> was automatically rejected as the assigned driver did not respond in time.", request.getId())
            );
        }

        webSocketHandler.sendUserNotification(request.getSenderUserId(), request.getId() + ": Auto rejected due to no response.");
    }
}
