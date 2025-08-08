package com.gdc.requests_management.feign.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentStatusResponseDto {
    private UUID paymentId;
    private UUID requestId;
    private String status; // e.g. SUCCESS, FAILED, PENDING
    private String message;
}
