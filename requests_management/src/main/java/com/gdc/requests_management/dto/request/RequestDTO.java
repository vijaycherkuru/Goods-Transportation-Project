package com.gdc.requests_management.dto.request;

import com.gdc.requests_management.model.enums.GoodsType;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequestDTO {

    @NotNull(message = "Sender user ID is required")
    private UUID senderUserId;

    @NotNull(message = "Ride user ID is required")
    private UUID rideUserId;

    @NotNull(message = "Ride ID is required")
    private UUID rideId;

    @NotBlank(message = "Goods description is required")
    private String goodsDescription;

    @NotNull(message = "Goods type is required")
    private GoodsType goodsType;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", inclusive = true)
    private Double weight;

    @NotNull(message = "Goods quantity is required")
    @Min(value = 1, message = "Goods quantity must be at least 1")
    private Integer goodsQuantity;

    @NotNull(message = "Required space is required")
    private RequiredSpaceType requiredSpace;

    @NotBlank(message = "From location is required")
    private String from;

    @NotBlank(message = "To location is required")
    private String to;

    private BigDecimal fare;

    private String specialInstructions;

    private LocalDateTime deliveryDate;
}
