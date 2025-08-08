package com.gdc.requests_management.model.entity;

import com.gdc.requests_management.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime statusTimestamp;

    private String notes;

    private UUID changedBy; // userId of the one who made the change
}
