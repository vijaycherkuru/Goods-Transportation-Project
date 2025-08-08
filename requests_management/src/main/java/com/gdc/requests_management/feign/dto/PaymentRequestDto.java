package com.gdc.requests_management.feign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDto {
    private UUID userId;
    private UUID requestId;
    private BigDecimal amount;
    private String paymentMethod; // e.g. UPI, CARD, etc.
}
