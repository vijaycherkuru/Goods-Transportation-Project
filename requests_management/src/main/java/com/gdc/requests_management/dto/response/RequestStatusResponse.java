package com.gdc.requests_management.dto.response;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestStatusResponse {
    private UUID requestId;
    private RequestStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
