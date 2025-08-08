package com.gdc.requests_management.dto.response;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSummaryResponse {

    private int totalRequests;
    private long pending;
    private long accepted;
    private long inTransit;
    private long delivered;
    private long cancelled;
}
