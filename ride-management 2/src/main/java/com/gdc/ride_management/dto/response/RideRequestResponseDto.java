package com.gdc.ride_management.dto.response;

import com.gdc.ride_management.enums.GoodsType;
import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.RequestStatus;
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
    private int goodsQuantity;
    private UUID senderUserId; // Reference to user-service
    private String username;
    private double fromLatitude;
    private double fromLongitude;
    private double toLatitude;
    private double toLongitude;
    private Timestamp fareCreatedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}