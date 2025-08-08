package com.gdc.requests_management.dto.request;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.Data;

@Data
public class RequestFilterDTO {
    private String from;
    private String to;
    private RequestStatus status;
}
