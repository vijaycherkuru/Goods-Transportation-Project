package com.gdc.requests_management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionReport {
    private String fromDate;
    private String toDate;
    private int totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal commissionEarned;
}