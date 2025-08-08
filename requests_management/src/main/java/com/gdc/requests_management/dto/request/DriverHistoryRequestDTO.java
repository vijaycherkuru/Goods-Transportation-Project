package com.gdc.requests_management.dto.request;


import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.Data;

@Data
public class DriverHistoryRequestDTO {
    private String fromDate;
    private String toDate;
    private RequestStatus status;
}