package com.gdc.requests_management.feign.dto;

import com.gdc.requests_management.model.enums.GoodsType;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestResponseDto {

    private RequestStatus requestStatus;
    private UUID id;
    private UUID rideId;
    private String from;
    private String to;
    private LocalDate date;
    private GoodsType goodsType;
    private Double goodsWeightInKg;
    private RequiredSpaceType requiredSpace;
    private String phone;
    private Double fare;
    private UUID senderUserId; // Reference to user-service
    private String displayName;
    private double fromLatitude;
    private double fromLongitude;
    private double toLatitude;
    private double toLongitude;
    private Timestamp fareCreatedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}