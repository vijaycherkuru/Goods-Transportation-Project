package com.gdc.requests_management.client;

import com.gdc.requests_management.feign.dto.RideResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "ride-management") // Eureka service name
public interface RideServiceClient {

    @GetMapping("/api/v1/rides/search")
    List<RideResponseDto> searchAvailableRides(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );


    @GetMapping("/api/v1/rides/{id}")
    RideResponseDto getRideById(@PathVariable("id") UUID rideId); // ✅ This must return RideResponseDTO
}
