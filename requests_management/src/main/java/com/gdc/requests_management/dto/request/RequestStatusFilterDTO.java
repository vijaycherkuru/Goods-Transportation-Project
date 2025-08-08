package com.gdc.requests_management.dto.request;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.Data;

@Data
public class RequestStatusFilterDTO {
    private RequestStatus status;
}