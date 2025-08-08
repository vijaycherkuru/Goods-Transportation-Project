package com.gdc.requests_management.client;

import com.gdc.requests_management.feign.dto.PaymentRequestDto;
import com.gdc.requests_management.feign.dto.PaymentStatusResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/process")
    void processPayment(@RequestBody PaymentRequestDto request);

    @GetMapping("/api/v1/payments/status/{requestId}")
    PaymentStatusResponseDto getPaymentStatus(@PathVariable UUID requestId);
}
