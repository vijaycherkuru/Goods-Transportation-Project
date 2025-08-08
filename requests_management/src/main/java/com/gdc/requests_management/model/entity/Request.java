package com.gdc.requests_management.model.entity;

import com.gdc.requests_management.model.enums.GoodsType;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID senderUserId;

    @Column(nullable = false)
    private UUID rideUserId;

    @Column(nullable = false)
    private UUID rideId;

    @Column
    private String goodsDescription;

    @Enumerated(EnumType.STRING)
    private GoodsType goodsType;

    @Column
    private Double weight;

    @Column(name = "goods_quantity")
    private Integer goodsQuantity;

    @Enumerated(EnumType.STRING)
    private RequiredSpaceType requiredSpace;

    @Column(name = "from_location", nullable = false)
    private String from;

    @Column(name = "to_location", nullable = false)
    private String to;

    @Column
    private BigDecimal fare;

    @Column
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime acceptedAt;

    @Column
    private LocalDateTime pickedUpAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime deliveryDate;

    @Column
    private Double fromLatitude;

    @Column
    private Double fromLongitude;

    @Column
    private Double toLatitude;

    @Column
    private Double toLongitude;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
