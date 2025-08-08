package com.gdc.ride_management.dto.request;

import com.gdc.ride_management.enums.GoodsType;
import com.gdc.ride_management.enums.RequiredSpaceType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestRequestDto {

    @NotBlank(message = "From location is required")
    private String from;

    @NotBlank(message = "To location is required")
    private String to;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Goods type is required")
    private GoodsType goodsType;

    @NotNull(message = "Goods weight is required")
    @Positive(message = "Goods weight must be greater than 0")
    private Double goodsWeightInKg;

    @NotNull(message = "Goods quantity is required")
    @Positive(message = "Goods quantity must be greater than 0")
    private Integer goodsQuantity;

    @NotNull(message = "Required space is required")
    private RequiredSpaceType requiredSpace;


    private UUID rideId;
}

