package com.gdc.requests_management.dto.request;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SearchRidesRequestDTO {
    private String from;
    private String to;
    private LocalDate date;
}
