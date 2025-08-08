package com.gdc.requests_management.utils;

import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.dto.response.RequestResponse;
import com.gdc.requests_management.feign.dto.RideRequestResponseDto;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;

import java.time.LocalDateTime;

public class RequestMapper {

    public static Request toEntityFromRideRequest(RequestDTO dto, RideRequestResponseDto rideRequestDto) {
        Request request = new Request();
        request.setRideId(dto.getRideId());
        request.setRideUserId(dto.getRideUserId());
        request.setGoodsDescription(dto.getGoodsDescription());
        request.setGoodsType(dto.getGoodsType());
        request.setWeight(dto.getWeight());
        request.setGoodsQuantity(dto.getGoodsQuantity()); // ✅ added
        request.setFrom(dto.getFrom());
        request.setTo(dto.getTo());

        request.setFromLatitude(rideRequestDto.getFromLatitude());
        request.setFromLongitude(rideRequestDto.getFromLongitude());
        request.setToLatitude(rideRequestDto.getToLatitude());
        request.setToLongitude(rideRequestDto.getToLongitude());

        request.setFare(dto.getFare());
        request.setRequiredSpace(dto.getRequiredSpace());
        request.setDeliveryDate(dto.getDeliveryDate());
        request.setSpecialInstructions(dto.getSpecialInstructions());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return request;
    }

    public static RequestResponse toDto(Request request) {
        RequestResponse dto = new RequestResponse();
        dto.setId(request.getId());
        dto.setSenderUserId(request.getSenderUserId());
        dto.setRideUserId(request.getRideUserId());
        dto.setRideId(request.getRideId());
        dto.setGoodsDescription(request.getGoodsDescription());
        dto.setGoodsType(request.getGoodsType());
        dto.setWeight(request.getWeight());
        dto.setGoodsQuantity(request.getGoodsQuantity()); // ✅ added
        dto.setFrom(request.getFrom());
        dto.setTo(request.getTo());
        dto.setFromLatitude(request.getFromLatitude());
        dto.setFromLongitude(request.getFromLongitude());
        dto.setToLatitude(request.getToLatitude());
        dto.setToLongitude(request.getToLongitude());
        dto.setFare(request.getFare());
        dto.setRequiredSpace(request.getRequiredSpace());
        dto.setDeliveryDate(request.getDeliveryDate());
        dto.setSpecialInstructions(request.getSpecialInstructions());
        dto.setStatus(request.getStatus());
        dto.setAcceptedAt(request.getAcceptedAt());
        dto.setPickedUpAt(request.getPickedUpAt());
        dto.setDeliveredAt(request.getDeliveredAt());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        // These will be filled later via Feign
        dto.setSenderName(null);
        dto.setDriverName(null);
        dto.setDriverPhoneNumber(null);

        return dto;
    }
}
