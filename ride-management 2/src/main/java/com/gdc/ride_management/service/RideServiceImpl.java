package com.gdc.ride_management.service;

import com.gdc.ride_management.client.UserClient;
import com.gdc.ride_management.dto.request.RideRequestDto;
import com.gdc.ride_management.dto.response.RideResponseDto;
import com.gdc.ride_management.dto.response.RideSearchResponseDto;
import com.gdc.ride_management.dto.response.StandardResponse;
import com.gdc.ride_management.dto.response.UserResponseDTO;
import com.gdc.ride_management.entity.Ride;
import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.RideStatus;
import com.gdc.ride_management.enums.VehicleType;
import com.gdc.ride_management.repository.RideRepository;
import com.gdc.ride_management.util.FareUtils;
import com.gdc.ride_management.util.OSMGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserClient userClient;
    private final OSMGeocodingService osmGeocodingService;

    @Override
    @Transactional
    public RideResponseDto createRide(UUID driverUserId, RideRequestDto requestDto) {
        double[] fromCoords = {0.0, 0.0};
        double[] toCoords = {0.0, 0.0};

        try {
            fromCoords = osmGeocodingService.getCoordinatesFromLocation(requestDto.getFrom());
        } catch (Exception e) {
            System.err.println("Failed to geocode 'from' location: " + e.getMessage());
        }

        try {
            toCoords = osmGeocodingService.getCoordinatesFromLocation(requestDto.getTo());
        } catch (Exception e) {
            System.err.println("Failed to geocode 'to' location: " + e.getMessage());
        }

        Ride ride = Ride.builder()
                .from(requestDto.getFrom())
                .to(requestDto.getTo())
                .date(requestDto.getDate())
                .time(requestDto.getTime())
                .rideStatus(RideStatus.AVAILABLE)
                .vehicleType(requestDto.getVehicleType())
                .luggageSpace(requestDto.getLuggageSpace())
                .drivingLicenseNumber(requestDto.getDrivingLicenseNumber())
                .rideUserId(driverUserId)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .fromLatitude(fromCoords[0])
                .fromLongitude(fromCoords[1])
                .toLatitude(toCoords[0])
                .toLongitude(toCoords[1])
                .build();

        ride = rideRepository.save(ride);
        return mapToRideResponseDto(ride);
    }

    @Override
    public RideResponseDto getRideById(UUID id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + id));
        return mapToRideResponseDto(ride);
    }

    @Override
    public List<RideResponseDto> getAllRides() {
        return rideRepository.findAll().stream()
                .map(this::mapToRideResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRide(UUID id) {
        if (!rideRepository.existsById(id)) {
            throw new RuntimeException("Ride not found with ID: " + id);
        }
        rideRepository.deleteById(id);
    }

    @Override
    public List<RideResponseDto> searchAvailableRides(String from, String to, LocalDate date) {
        return rideRepository.findByFromIgnoreCaseAndToIgnoreCaseAndDate(from, to, date).stream()
                .map(this::mapToRideResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideSearchResponseDto> searchAvailableRidesWithFare(
            String from,
            String to,
            LocalDate date,
            RequiredSpaceType luggageSpace,
            double goodsWeightInKg,
            int goodsQuantity
    ) {
        double[] fromCoords = osmGeocodingService.getCoordinatesFromLocation(from);
        double[] toCoords = osmGeocodingService.getCoordinatesFromLocation(to);
        double distance = FareUtils.calculateDistance(fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]);

        return rideRepository
                .findByFromIgnoreCaseAndToIgnoreCaseAndDateAndRideStatus(from, to, date, RideStatus.AVAILABLE)
                .stream()
                .map(ride -> {
                    double totalCapacityInKg = getCapacityFromEnum(ride.getLuggageSpace());

                    double bookedWeight = ride.getRideRequests() != null
                            ? ride.getRideRequests().stream()
                            .mapToDouble(req -> req.getGoodsWeightInKg() != null ? req.getGoodsWeightInKg() : 0.0)
                            .sum()
                            : 0.0;

                    double availableSpace = totalCapacityInKg - bookedWeight;
                    double estimatedFare = FareUtils.calculateFare(distance, goodsWeightInKg, goodsQuantity);

                    UserResponseDTO userDto = null;
                    try {
                        StandardResponse<UserResponseDTO> response = userClient.getUserDetails(ride.getRideUserId());
                        if (response != null && response.getData() != null) {
                            userDto = response.getData();
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Failed to fetch user info for ride userId " + ride.getRideUserId() + ": " + e.getMessage());
                    }

                    return RideSearchResponseDto.builder()
                            .rideId(ride.getId())
                            .from(ride.getFrom())
                            .to(ride.getTo())
                            .date(ride.getDate())
                            .time(ride.getTime())
                            .vehicleType(VehicleType.valueOf(ride.getVehicleType()))
                            .luggageSpace(ride.getLuggageSpace())
                            .availableSpaceInKg(availableSpace)
                            .estimatedFare(estimatedFare)
                            .rideUserId(ride.getRideUserId())
                            .username(userDto != null ? userDto.getDisplayName() : null)
                            .phone(userDto != null ? userDto.getPhone() : null)
                            .goodsQuantity(goodsQuantity)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponseDto> getRidesByDriverId(UUID driverUserId) {
        return rideRepository.findByRideUserId(driverUserId).stream()
                .map(this::mapToRideResponseDto)
                .collect(Collectors.toList());
    }

    private RideResponseDto mapToRideResponseDto(Ride ride) {
        UserResponseDTO userDto = null;
        double totalCapacityInKg = getCapacityFromEnum(ride.getLuggageSpace());
        try {
            StandardResponse<UserResponseDTO> response = userClient.getUserDetails(ride.getRideUserId());
            if (response != null && response.getData() != null) {
                userDto = response.getData();
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch user info for ride userId " + ride.getRideUserId() + ": " + e.getMessage());
        }

        double bookedWeight = ride.getRideRequests() != null
                ? ride.getRideRequests().stream()
                .mapToDouble(req -> req.getGoodsWeightInKg() != null ? req.getGoodsWeightInKg() : 0.0)
                .sum()
                : 0.0;

        double availableSpace = totalCapacityInKg - bookedWeight;

        return RideResponseDto.builder()
                .id(ride.getId())
                .from(ride.getFrom())
                .to(ride.getTo())
                .date(ride.getDate())
                .time(ride.getTime())
                .rideStatus(ride.getRideStatus())
                .vehicleType(ride.getVehicleType())
                .luggageSpace(ride.getLuggageSpace())
                .drivingLicenseNumber(ride.getDrivingLicenseNumber())
                .rideUserId(ride.getRideUserId())
                .userName(userDto != null ? userDto.getDisplayName() : null)
                .phone(userDto != null ? userDto.getPhone() : null)
                .fromLatitude(ride.getFromLatitude())
                .fromLongitude(ride.getFromLongitude())
                .toLatitude(ride.getToLatitude())
                .toLongitude(ride.getToLongitude())
                .createdAt(ride.getCreatedAt())
                .updatedAt(ride.getUpdatedAt())
                .build();
    }

    private double getCapacityFromEnum(RequiredSpaceType spaceType) {
        return switch (spaceType) {
            case SMALL -> 50.0;
            case MEDIUM -> 100.0;
            case LARGE -> 150.0;
            case EXTRA_LARGE -> 200.0;
        };
    }
}
