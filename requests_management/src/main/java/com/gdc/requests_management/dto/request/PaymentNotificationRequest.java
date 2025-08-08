package com.gdc.requests_management.dto.request;


import java.util.UUID;

public  class PaymentNotificationRequest {
    public UUID senderUserId;
    public UUID rideUserId;
    public String requestId;
    public String paymentStatus; // e.g., "SUCCESS", "FAILED"
    public String amount;
}