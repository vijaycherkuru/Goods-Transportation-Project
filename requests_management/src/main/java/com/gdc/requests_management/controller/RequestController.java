package com.gdc.requests_management.controller;

import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.request.*;
import com.gdc.requests_management.dto.response.RequestResponse;
import com.gdc.requests_management.dto.response.RequestStatusResponse;
import com.gdc.requests_management.dto.response.RequestSummaryResponse;
import com.gdc.requests_management.dto.response.StandardResponse;
import com.gdc.requests_management.dto.response.TransactionReport;
import com.gdc.requests_management.feign.dto.UserResponseDTO;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.service.RequestService;
import com.gdc.requests_management.utils.RequestMapper;
import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gdc.requests_management.utils.EmailTokenValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Request Management", description = "APIs for managing transport requests in the Peer-to-Peer Goods Transportation System")
public class RequestController {


    @Autowired
    private RequestService requestService;

    @Autowired
    private WebSocketNotificationHandler webSocketNotificationHandler;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private EmailTokenValidator emailTokenValidator;


    @Operation(
            summary = "Create a transport request",
            description = "Allows a sender to create a new transport request for goods.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User is banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<StandardResponse<RequestResponse>> createRequest(
            @Valid @RequestBody RequestDTO requestDTO,
            @AuthenticationPrincipal String userId) {
        Request createdRequest = requestService.createRequest(requestDTO, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(createdRequest);
        return new ResponseEntity<>(StandardResponse.created(response, "Transport request created successfully"), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get request by ID",
            description = "Retrieves details of a specific request by its ID.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{requestId}")
    public ResponseEntity<StandardResponse<RequestResponse>> getRequestById(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        Request request = requestService.getRequestById(requestId, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Request retrieved successfully"));
    }

    @Operation(
            summary = "Update a request",
            description = "Updates an existing request's details.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data or request not pending",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only sender can update or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{requestId}")
    public ResponseEntity<StandardResponse<RequestResponse>> updateRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody RequestUpdateDTO updateDTO,
            @AuthenticationPrincipal String userId) {
        Request updatedRequest = requestService.updateRequest(requestId, updateDTO, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(updatedRequest);
        return ResponseEntity.ok(StandardResponse.success(response, "Request updated successfully"));
    }

    @Operation(
            summary = "Cancel a request",
            description = "Cancels a pending request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request not pending",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only sender can cancel or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{requestId}")
    public ResponseEntity<StandardResponse<Void>> cancelRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        requestService.cancelRequest(requestId, UUID.fromString(userId));
        return ResponseEntity.ok(StandardResponse.success(null, "Request cancelled successfully"));
    }

    @Operation(
            summary = "Get user's requests",
            description = "Retrieves a paginated list of the authenticated user's requests.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/my-requests")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getMyRequests(
            @AuthenticationPrincipal String userId,
            Pageable pageable,
            @RequestBody(required = false) RequestStatusFilterDTO statusFilter) {
        RequestStatus status = statusFilter != null ? statusFilter.getStatus() : null;
        Page<Request> requests = requestService.getRequestsByUser(UUID.fromString(userId), pageable, status);
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "User requests retrieved successfully"));
    }

    @Operation(
            summary = "Get request summary",
            description = "Retrieves a summary of the authenticated user's requests by status.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request summary retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/my-requests/summary")
    public ResponseEntity<StandardResponse<RequestSummaryResponse>> getMyRequestsSummary(
            @AuthenticationPrincipal String userId) {
        RequestSummaryResponse summary = requestService.getRequestsSummary(UUID.fromString(userId));
        return ResponseEntity.ok(StandardResponse.success(summary, "Request summary retrieved successfully"));
    }

    @Operation(
            summary = "Get requests for a ride",
            description = "Retrieves all requests associated with a specific ride ID.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests for ride retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/ride")
    public ResponseEntity<StandardResponse<List<RequestResponse>>> getRequestsForRide(
            @RequestBody RideIdRequestDTO rideIdRequest,
            @AuthenticationPrincipal String userId) {
        List<Request> requests = requestService.getRequestsForRide(rideIdRequest.getRideId());
        List<RequestResponse> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(response, "Requests for ride retrieved successfully"));
    }

    @Operation(
            summary = "Get assigned requests",
            description = "Retrieves a paginated list of requests assigned to the authenticated driver.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assigned requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/driver/assigned")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getAssignedRequests(
            @AuthenticationPrincipal String userId,
            Pageable pageable,
            @RequestBody(required = false) RequestStatusFilterDTO statusFilter) {
        RequestStatus status = statusFilter != null ? statusFilter.getStatus() : null;
        Page<Request> requests = requestService.getRequestsByRideUser(UUID.fromString(userId), pageable, status);
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "Assigned requests retrieved successfully"));
    }

    @Operation(
            summary = "Get active requests for driver",
            description = "Retrieves all active (IN_TRANSIT) requests for the authenticated driver.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/driver/active")
    public ResponseEntity<StandardResponse<List<RequestResponse>>> getActiveRequestsForDriver(
            @AuthenticationPrincipal String userId) {
        List<Request> requests = requestService.getActiveRequestsForRideUser(UUID.fromString(userId));
        List<RequestResponse> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(response, "Active requests retrieved successfully"));
    }

    @Operation(
            summary = "Accept a request",
            description = "Allows a driver to accept a pending request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request accepted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request not pending or already assigned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    }
    )
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<StandardResponse<RequestResponse>> acceptRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId
    ) {
        Request updated = requestService.acceptRequest(requestId, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(updated);
        webSocketNotificationHandler.sendRealTimeUpdate(updated.getSenderUserId(), requestId + ":Request accepted");
        return ResponseEntity.ok(StandardResponse.success(response, "Request accepted successfully"));
    }
    @GetMapping("/{requestId}/accept")
    public ResponseEntity<StandardResponse<RequestResponse>> acceptRequestWithToken(
            @PathVariable UUID requestId,
            @RequestParam("token") String token
    ) {
        emailTokenValidator.validateToken(token, requestId);
        UUID driverId = emailTokenValidator.extractRideUserId(token);
        Request updated = requestService.acceptRequest(requestId, driverId);
        RequestResponse response = convertToResponseDTO(updated);
        webSocketNotificationHandler.sendRealTimeUpdate(updated.getSenderUserId(), requestId + ":Request accepted");
        return ResponseEntity.ok(StandardResponse.success(response, "Request accepted successfully"));
    }

    @Operation(
            summary = "Reject a request via token",
            description = "Allows a driver to reject a request via secure token (from email)."
    )
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<StandardResponse<RequestResponse>> rejectRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId
    ) {
        Request updated = requestService.rejectRequest(requestId, UUID.fromString(userId), "Rejected via frontend");
        RequestResponse response = convertToResponseDTO(updated);
        webSocketNotificationHandler.sendRealTimeUpdate(updated.getSenderUserId(), requestId + ":Request rejected");
        return ResponseEntity.ok(StandardResponse.success(response, "Request rejected successfully"));
    }

    @GetMapping("/{requestId}/reject")
    public ResponseEntity<StandardResponse<RequestResponse>> rejectRequestWithToken(
            @PathVariable UUID requestId,
            @RequestParam("token") String token
    ) {
        emailTokenValidator.validateToken(token, requestId);
        UUID driverId = emailTokenValidator.extractRideUserId(token);
        Request updated = requestService.rejectRequest(requestId, driverId, "Rejected via email link");
        RequestResponse response = convertToResponseDTO(updated);
        webSocketNotificationHandler.sendRealTimeUpdate(updated.getSenderUserId(), requestId + ":Request rejected");
        return ResponseEntity.ok(StandardResponse.success(response, "Request rejected successfully"));
    }

    @Operation(
            summary = "Mark request as picked up",
            description = "Marks an accepted request as picked up.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request marked as picked up successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request not accepted or not assigned to driver",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{requestId}/pickup")

    public ResponseEntity<StandardResponse<RequestResponse>> markAsPickedUp(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        Request updatedRequest = requestService.markAsPickedUp(requestId, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(updatedRequest);
        return ResponseEntity.ok(StandardResponse.success(response, "Request marked as picked up successfully"));
    }

    @Operation(
            summary = "Mark request as delivered",
            description = "Marks an in-transit request as delivered.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request marked as delivered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request not in-transit or not assigned to driver",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Payment processing failed",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{requestId}/deliver")
    public ResponseEntity<StandardResponse<RequestResponse>> markAsDelivered(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String deliveryNotes,
            @AuthenticationPrincipal String userId) {
        Request updatedRequest = requestService.markAsDelivered(requestId, UUID.fromString(userId), deliveryNotes);
        RequestResponse response = convertToResponseDTO(updatedRequest);
        return ResponseEntity.ok(StandardResponse.success(response, "Request marked as delivered successfully"));
    }

    @Operation(
            summary = "Get request status",
            description = "Retrieves the current status and timestamps of a specific request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{requestId}/status")
    public ResponseEntity<StandardResponse<RequestStatusResponse>> getRequestStatus(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        RequestStatusResponse statusResponse = requestService.getRequestStatus(requestId, UUID.fromString(userId));
        return ResponseEntity.ok(StandardResponse.success(statusResponse, "Request status retrieved successfully"));
    }

    @Operation(
            summary = "Get request history",
            description = "Retrieves a paginated history of the authenticated user's requests.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request history retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/history")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getRequestHistory(
            @AuthenticationPrincipal String userId,
            Pageable pageable,
            @RequestBody(required = false) DateRangeRequestDTO dateRange) {
        String fromDate = dateRange != null ? dateRange.getFromDate() : null;
        String toDate = dateRange != null ? dateRange.getToDate() : null;
        Page<Request> requests = requestService.getRequestHistory(UUID.fromString(userId), pageable, fromDate, toDate);
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "Request history retrieved successfully"));
    }

    @Operation(
            summary = "Get driver request history",
            description = "Retrieves a paginated history of requests assigned to the authenticated driver.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver request history retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format or status",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/driver/history")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getDriverRequestHistory(
            @AuthenticationPrincipal String userId,
            Pageable pageable,
            @RequestBody DriverHistoryRequestDTO request) {
        Page<Request> requests = requestService.getRideUserRequestHistory(
                UUID.fromString(userId), pageable, request.getFromDate(), request.getToDate(), request.getStatus());
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "Driver request history retrieved successfully"));
    }

    @Operation(
            summary = "Get detailed request history",
            description = "Retrieves detailed history of a specific request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed request history retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{requestId}/history")
    public ResponseEntity<StandardResponse<RequestResponse>> getRequestDetailedHistory(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        Request request = requestService.getRequestDetailedHistory(requestId, UUID.fromString(userId));
        RequestResponse response = convertToResponseDTO(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Detailed request history retrieved successfully"));
    }

    @Operation(
            summary = "Get completed requests history",
            description = "Retrieves a paginated history of completed or cancelled requests.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed requests history retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/history/completed")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getCompletedRequestsHistory(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        Page<Request> requests = requestService.getCompletedRequestsHistory(UUID.fromString(userId), pageable);
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "Completed requests history retrieved successfully"));
    }

    @Operation(
            summary = "Search requests",
            description = "Searches requests based on filters like location, priority, and status.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/search")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> searchRequests(
            @Valid @RequestBody RequestFilterDTO filterDTO,
            Pageable pageable,
            @AuthenticationPrincipal String userId) {
        log.info("Received search request: {}", filterDTO);
        Page<Request> requests = requestService.searchRequests(filterDTO, pageable, UUID.fromString(userId));
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "Requests retrieved successfully"));
    }

    @Operation(
            summary = "Get requests by location",
            description = "Retrieves requests based on pickup and delivery locations within a radius.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location-based requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid location or radius",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: User banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/location")
    public ResponseEntity<StandardResponse<List<RequestResponse>>> getRequestsByLocation(
            @Valid @RequestBody LocationSearchRequestDTO locationSearch,
            @AuthenticationPrincipal String userId) {
        List<Request> requests = requestService.getRequestsByLocation(
                locationSearch.getFrom(), locationSearch.getTo(),
                locationSearch.getRadius(), UUID.fromString(userId));
        List<RequestResponse> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(response, "Location-based requests retrieved successfully"));
    }

    @Operation(
            summary = "Get all requests (Admin)",
            description = "Retrieves a paginated list of all requests, optionally filtered by status.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/admin/all")
    public ResponseEntity<StandardResponse<Page<RequestResponse>>> getAllRequests(
            Pageable pageable,
            @RequestBody(required = false) RequestStatusFilterDTO statusFilter) {
        RequestStatus status = statusFilter != null ? statusFilter.getStatus() : null;
        Page<Request> requests = requestService.getAllRequests(pageable, status);
        Page<RequestResponse> response = requests.map(this::convertToResponseDTO);
        return ResponseEntity.ok(StandardResponse.success(response, "All requests retrieved successfully"));
    }

    @Operation(
            summary = "Get requests by user (Admin)",
            description = "Retrieves all requests for a specific user.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/admin/user")
    public ResponseEntity<StandardResponse<List<RequestResponse>>> getRequestsByUserAdmin(
            @RequestBody UserIdRequestDTO userIdRequest) {
        List<Request> requests = requestService.getRequestsByUserAdmin(userIdRequest.getUserId());
        List<RequestResponse> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(response, "User requests retrieved successfully"));
    }

    @Operation(
            summary = "Update driver location",
            description = "Allows a driver to update their current location.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid location data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/driver/location")
    public ResponseEntity<StandardResponse<Void>> updateDriverLocation(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody LocationDTO locationDTO) {
        requestService.updateRideUserLocation(UUID.fromString(userId), locationDTO);
        return ResponseEntity.ok(StandardResponse.success(null, "Location updated successfully"));
    }

    @Operation(
            summary = "Update request tracking",
            description = "Allows a driver to update tracking information for a request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or tracking data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Driver role required or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{requestId}/tracking")
    public ResponseEntity<StandardResponse<Void>> updateRequestTracking(
            @PathVariable UUID requestId,
            @Valid @RequestBody LocationDTO locationDTO,
            @AuthenticationPrincipal String userId) {
        requestService.updateRequestTracking(requestId, locationDTO, UUID.fromString(userId));
        return ResponseEntity.ok(StandardResponse.success(null, "Tracking updated successfully"));
    }

    @Operation(
            summary = "Get request tracking",
            description = "Retrieves the current tracking information for a request.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Access denied or user banned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Request or tracking not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{requestId}/tracking")
    public ResponseEntity<StandardResponse<LocationDTO>> getRequestTracking(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal String userId) {
        LocationDTO tracking = requestService.getRequestTracking(requestId, UUID.fromString(userId));
        return ResponseEntity.ok(StandardResponse.success(tracking, "Tracking retrieved successfully"));
    }

    @Operation(
            summary = "Ban a user (Admin)",
            description = "Initiates a user ban.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User ban initiated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/admin/user/{userId}/ban")
    public ResponseEntity<StandardResponse<Void>> banUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {
        requestService.banUser(userId, reason);
        return ResponseEntity.ok(StandardResponse.success(null, "User ban initiated successfully"));
    }

    @Operation(
            summary = "Get transaction report (Admin)",
            description = "Generates a transaction report for a specified date range.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction report generated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/admin/reports/transactions")
    public ResponseEntity<StandardResponse<TransactionReport>> getTransactionReport(
            @RequestBody DateRangeRequestDTO dateRange) {
        TransactionReport report = requestService.generateTransactionReport(dateRange.getFromDate(), dateRange.getToDate());
        return ResponseEntity.ok(StandardResponse.success(report, "Transaction report generated successfully"));
    }

    @Operation(
            summary = "Handle WebSocket request updates",
            description = "Receives real-time request updates via WebSocket and broadcasts them to subscribed clients.")
    @MessageMapping("/request.update")
    @SendTo("/topic/request-updates")
    public String handleRequestUpdate(@RequestBody String message) {
        return message;
    }

    private RequestResponse convertToResponseDTO(Request request) {
        RequestResponse dto = RequestMapper.toDto(request);

        // 🔁 Sender Info
        if (request.getSenderUserId() != null) {
            try {
                UserResponseDTO user = userServiceClient.getUserDetails(request.getSenderUserId()).getData();
                dto.setSenderName(user.getDisplayName());
            } catch (Exception e) {
                dto.setSenderName("Unknown");
            }
        }

// 🔁 Driver Info
        if (request.getRideUserId() != null) {
            try {
                UserResponseDTO driver = userServiceClient.getUserDetails(request.getRideUserId()).getData();
                dto.setDriverName(driver.getDisplayName());
                dto.setDriverPhoneNumber(driver.getPhoneNumber());
            } catch (Exception e) {
                dto.setDriverName("Unknown");
                dto.setDriverPhoneNumber("N/A");
            }
        }


        return dto;
    }

}