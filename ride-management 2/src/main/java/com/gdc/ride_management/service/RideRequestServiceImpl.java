package com.gdc.ride_management.service;

import com.gdc.ride_management.client.UserClient;
import com.gdc.ride_management.dto.request.RideRequestRequestDto;
import com.gdc.ride_management.dto.response.RideRequestResponseDto;
import com.gdc.ride_management.dto.response.StandardResponse;
import com.gdc.ride_management.dto.response.UserResponseDTO;
import com.gdc.ride_management.entity.Ride;
import com.gdc.ride_management.entity.RideRequest;
import com.gdc.ride_management.enums.RequestStatus;
import com.gdc.ride_management.repository.RideRepository;
import com.gdc.ride_management.repository.RideRequestRepository;
import com.gdc.ride_management.util.FareUtils;
import com.gdc.ride_management.util.OSMGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideRequestServiceImpl implements RideRequestService {

    private final RideRequestRepository rideRequestRepository;
    private final RideRepository rideRepository;
    private final OSMGeocodingService osmGeocodingService;
    private final UserClient userClient;

    @Override
    @Transactional
    public RideRequestResponseDto createRideRequest(RideRequestRequestDto requestDto, UUID senderUserId) {
        Ride ride = rideRepository.findById(requestDto.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + requestDto.getRideId()));

        double[] sourceCoords = osmGeocodingService.getCoordinatesFromLocation(requestDto.getFrom());
        double[] destinationCoords = osmGeocodingService.getCoordinatesFromLocation(requestDto.getTo());

        double distance = FareUtils.calculateDistance(sourceCoords[0], sourceCoords[1], destinationCoords[0], destinationCoords[1]);
        double fare = FareUtils.calculateFare(distance, requestDto.getGoodsWeightInKg(), requestDto.getGoodsQuantity());

        double maxCapacity = getCapacityFromEnum(ride.getLuggageSpace());
        double usedCapacity = rideRequestRepository
                .findByRideIdAndRequestStatus(ride.getId(), RequestStatus.ACCEPTED)
                .stream()
                .mapToDouble(RideRequest::getGoodsWeightInKg)
                .sum();
        double availableCapacity = maxCapacity - usedCapacity;

        if (requestDto.getGoodsWeightInKg() > availableCapacity) {
            throw new RuntimeException("Only " + availableCapacity + "kg space left. Cannot book " + requestDto.getGoodsWeightInKg() + "kg.");
        }

        UserResponseDTO senderUserDto = extractUserData(senderUserId);

        RideRequest rideRequest = RideRequest.builder()
                .senderUserId(senderUserId)
                .ride(ride)
                .from(requestDto.getFrom())
                .to(requestDto.getTo())
                .date(requestDto.getDate())
                .goodsType(requestDto.getGoodsType())
                .goodsWeightInKg(requestDto.getGoodsWeightInKg())
                .requiredSpace(requestDto.getRequiredSpace())
                .phoneNumber(senderUserDto != null ? senderUserDto.getPhone() : null)
                .fare(fare)
                .fareCreatedAt(new Timestamp(System.currentTimeMillis()))
                .requestStatus(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .fromLatitude(sourceCoords[0])
                .fromLongitude(sourceCoords[1])
                .toLatitude(destinationCoords[0])
                .toLongitude(destinationCoords[1])
                .build();

        rideRequest = rideRequestRepository.save(rideRequest);

        return mapToRideRequestResponseDto(rideRequest, senderUserDto, sourceCoords, destinationCoords);
    }

    private double getCapacityFromEnum(Enum<?> luggageSpaceEnum) {
        return switch (luggageSpaceEnum.name()) {
            case "SMALL" -> 20.0;
            case "MEDIUM" -> 50.0;
            case "LARGE" -> 100.0;
            default -> throw new IllegalArgumentException("Unknown luggage space: " + luggageSpaceEnum);
        };
    }

    @Override
    public List<RideRequestResponseDto> getAllRideRequests() {
        return rideRequestRepository.findAll().stream()
                .map(request -> {
                    UserResponseDTO userDto = extractUserData(request.getSenderUserId());
                    return mapToRideRequestResponseDto(
                            request,
                            userDto,
                            new double[]{request.getFromLatitude(), request.getFromLongitude()},
                            new double[]{request.getToLatitude(), request.getToLongitude()}
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public RideRequestResponseDto getRideRequestById(UUID id) {
        RideRequest rideRequest = rideRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride Request not found with ID: " + id));

        UserResponseDTO senderUserDto = extractUserData(rideRequest.getSenderUserId());

        return mapToRideRequestResponseDto(
                rideRequest,
                senderUserDto,
                new double[]{rideRequest.getFromLatitude(), rideRequest.getFromLongitude()},
                new double[]{rideRequest.getToLatitude(), rideRequest.getToLongitude()}
        );
    }

    @Override
    @Transactional
    public RideRequestResponseDto updateRideRequestStatus(UUID id, String status, UUID currentUserId) {
        RideRequest rideRequest = rideRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride Request not found with ID: " + id));

        UUID rideOwnerId = rideRequest.getRide().getRideUserId();
        if (!rideOwnerId.equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to update this request's status");
        }

        rideRequest.setRequestStatus(RequestStatus.valueOf(status.toUpperCase()));
        rideRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        rideRequest = rideRequestRepository.save(rideRequest);

        UserResponseDTO senderUserDto = extractUserData(rideRequest.getSenderUserId());

        return mapToRideRequestResponseDto(
                rideRequest,
                senderUserDto,
                new double[]{rideRequest.getFromLatitude(), rideRequest.getFromLongitude()},
                new double[]{rideRequest.getToLatitude(), rideRequest.getToLongitude()}
        );
    }

    @Override
    @Transactional
    public RideRequestResponseDto updateStatus(UUID id, String status) {
        RideRequest rideRequest = rideRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride Request not found with ID: " + id));

        rideRequest.setRequestStatus(RequestStatus.valueOf(status.toUpperCase()));
        rideRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        rideRequest = rideRequestRepository.save(rideRequest);

        UserResponseDTO senderUserDto = extractUserData(rideRequest.getSenderUserId());

        return mapToRideRequestResponseDto(
                rideRequest,
                senderUserDto,
                new double[]{rideRequest.getFromLatitude(), rideRequest.getFromLongitude()},
                new double[]{rideRequest.getToLatitude(), rideRequest.getToLongitude()}
        );
    }

    @Override
    @Transactional
    public void deleteRideRequest(UUID id) {
        if (!rideRequestRepository.existsById(id)) {
            throw new RuntimeException("Ride Request not found with ID: " + id);
        }
        rideRequestRepository.deleteById(id);
    }

    @Override
    public List<RideRequestResponseDto> getRequestsBySenderId(UUID senderUserId) {
        return rideRequestRepository.findBySenderUserId(senderUserId).stream()
                .map(request -> {
                    UserResponseDTO userDto = extractUserData(request.getSenderUserId());
                    return mapToRideRequestResponseDto(
                            request,
                            userDto,
                            new double[]{request.getFromLatitude(), request.getFromLongitude()},
                            new double[]{request.getToLatitude(), request.getToLongitude()}
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public List<RideRequestResponseDto> getRequestsForRide(UUID rideUserId) {
        List<Ride> driversRides = rideRepository.findByRideUserId(rideUserId);
        List<UUID> rideIds = driversRides.stream().map(Ride::getId).toList();

        return rideRequestRepository.findByRideIdIn(rideIds).stream()
                .map(request -> {
                    UserResponseDTO userDto = extractUserData(request.getSenderUserId());
                    return mapToRideRequestResponseDto(
                            request,
                            userDto,
                            new double[]{request.getFromLatitude(), request.getFromLongitude()},
                            new double[]{request.getToLatitude(), request.getToLongitude()}
                    );
                }).collect(Collectors.toList());
    }

    private RideRequestResponseDto mapToRideRequestResponseDto(
            RideRequest rideRequest,
            UserResponseDTO senderUserDto,
            double[] fromCoords,
            double[] toCoords
    ) {
        return RideRequestResponseDto.builder()
                .id(rideRequest.getId())
                .senderUserId(rideRequest.getSenderUserId())
                .rideId(rideRequest.getRide().getId())
                .from(rideRequest.getFrom())
                .to(rideRequest.getTo())
                .date(rideRequest.getDate())
                .goodsType(rideRequest.getGoodsType())
                .goodsWeightInKg(rideRequest.getGoodsWeightInKg())
                .requiredSpace(rideRequest.getRequiredSpace())
                .phone(senderUserDto != null ? senderUserDto.getPhone() : null)
                .username(senderUserDto != null ? senderUserDto.getDisplayName() : null)
                .fare(rideRequest.getFare())
                .fareCreatedAt(rideRequest.getFareCreatedAt())
                .requestStatus(rideRequest.getRequestStatus())
                .createdAt(rideRequest.getCreatedAt())
                .updatedAt(rideRequest.getUpdatedAt())
                .fromLatitude(fromCoords[0])
                .fromLongitude(fromCoords[1])
                .toLatitude(toCoords[0])
                .toLongitude(toCoords[1])
                .build();
    }

    private UserResponseDTO extractUserData(UUID userId) {
        try {
            StandardResponse<UserResponseDTO> response = userClient.getUserDetails(userId);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch user info for userId " + userId + ": " + e.getMessage());
            return null;
        }
    }
}
