package com.gdc.requests_management.dto.request;

import com.gdc.requests_management.model.enums.GoodsType;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class RequestUpdateDTO {

    private String goodsDescription;

    private GoodsType goodsType;

    @DecimalMin(value = "0.1", inclusive = true)
    private Double weight;

    @DecimalMin(value = "1.0", inclusive = true, message = "Quantity must be at least 1")
    private Integer goodsQuantity; // ✅ New field

    private RequiredSpaceType requiredSpace;

    private String from;
    private String to;

    private String specialInstructions;

    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;
}
