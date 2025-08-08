package com.gdc.requests_management.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendDriverRequestNotification(
            String to,
            String requestId,
            String from,
            String toLocation,
            String goodsDescription,
            String fare,
            String acceptUrl,
            String rejectUrl
    );

    void sendUserConfirmation(
            String to,
            String requestId,
            String from,
            String toLocation,
            String goodsDescription
    );
}
