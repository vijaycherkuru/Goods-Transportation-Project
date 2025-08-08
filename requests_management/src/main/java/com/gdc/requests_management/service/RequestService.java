package com.gdc.requests_management.service;

import com.gdc.requests_management.dto.request.LocationDTO;
import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.dto.request.RequestFilterDTO;
import com.gdc.requests_management.dto.request.RequestUpdateDTO;
import com.gdc.requests_management.dto.response.RequestStatusResponse;
import com.gdc.requests_management.dto.response.RequestSummaryResponse;
import com.gdc.requests_management.dto.response.TransactionReport;
import com.gdc.requests_management.feign.dto.RideRequestResponseDto;
import com.gdc.requests_management.feign.dto.RideResponseDto;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RequestService {

    // RIDE SEARCH
    List<RideResponseDto> searchAvailableRides(String from, String to, LocalDate date);

    // REQUEST CORE
    Request createRequest(RequestDTO dto, UUID senderUserId);
    Request getRequestById(UUID requestId, UUID requesterUserId);
    Request updateRequest(UUID requestId, RequestUpdateDTO dto, UUID requesterUserId);
    void cancelRequest(UUID requestId, UUID requesterUserId);

    // USER VIEWS
    Page<Request> getRequestsByUser(UUID senderUserId, Pageable pageable, RequestStatus status);
    RequestSummaryResponse getRequestsSummary(UUID senderUserId);
    Page<Request> getCompletedRequestsHistory(UUID senderUserId, Pageable pageable);
    List<Request> getRequestsByUserAdmin(UUID userId);

    // REQUEST STATUS
    RequestStatusResponse getRequestStatus(UUID requestId, UUID userId);
    Request acceptRequest(UUID requestId, UUID rideUserId);
    Request rejectRequest(UUID requestId, UUID rideUserId, String reason);
    Request markAsPickedUp(UUID requestId, UUID rideUserId);
    Request markAsDelivered(UUID requestId, UUID rideUserId, String deliveryNotes);

    // HISTORY
    Page<Request> getRequestHistory(UUID userId, Pageable pageable, String from, String to);
    Page<Request> getRideUserRequestHistory(UUID rideUserId, Pageable pageable, String from, String to, RequestStatus status);
    Request getRequestDetailedHistory(UUID requestId, UUID userId);

    // REQUEST LISTING
    List<Request> getRequestsForRide(UUID rideId);
    Page<Request> getRequestsByRideUser(UUID rideUserId, Pageable pageable, RequestStatus status);
    List<Request> getActiveRequestsForRideUser(UUID rideUserId);
    Page<Request> getAllRequests(Pageable pageable, RequestStatus status);

    Page<Request> searchRequests(RequestFilterDTO filterDTO, Pageable pageable, UUID userId);
    List<Request> getRequestsByLocation(String pickup, String delivery, int radius, UUID userId);

    // RIDE USER TRACKING & LOCATION
    void updateRideUserLocation(UUID rideUserId, LocationDTO location);
    void updateRequestTracking(UUID requestId, LocationDTO location, UUID rideUserId);
    LocationDTO getRequestTracking(UUID requestId, UUID userId);

    // ADMIN
    TransactionReport generateTransactionReport(String fromDate, String toDate);
    void banUser(UUID userId, String reason);
}
