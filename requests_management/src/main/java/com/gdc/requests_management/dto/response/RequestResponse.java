package com.gdc.requests_management.dto.response;

import com.gdc.requests_management.model.enums.GoodsType;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequestResponse {

    private UUID id;

    private UUID senderUserId;
    private UUID rideUserId;
    private UUID rideId;

    private String goodsDescription;
    private GoodsType goodsType;
    private Double weight;
    private Integer goodsQuantity; // ✅ added
    private RequiredSpaceType requiredSpace;

    private String from;
    private String to;
    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;

    private BigDecimal fare;
    private String specialInstructions;
    private RequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime deliveryDate;

    private String rejectionReason;

    // Enriched fields from user-service
    private String driverName;
    private String driverPhoneNumber;
    private String senderName;
}
