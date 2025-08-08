package com.gdc.requests_management.service.impl;

import com.gdc.requests_management.client.PaymentServiceClient;
import com.gdc.requests_management.client.RideServiceClient;
import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.request.LocationDTO;
import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.dto.request.RequestFilterDTO;
import com.gdc.requests_management.dto.request.RequestUpdateDTO;
import com.gdc.requests_management.dto.response.RequestStatusResponse;
import com.gdc.requests_management.dto.response.RequestSummaryResponse;
import com.gdc.requests_management.dto.response.TransactionReport;
import com.gdc.requests_management.feign.dto.*;
import com.gdc.requests_management.service.*;
import com.gdc.requests_management.exception.*;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.entity.RequestHistory;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.repository.RequestHistoryRepository;
import com.gdc.requests_management.repository.RequestRepository;
import com.gdc.requests_management.utils.OSMGeocodingService;
import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final WebSocketNotificationHandler webSocketNotificationHandler;
    private final RideServiceClient rideServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final UserServiceClient userServiceClient;
    private final LocationService locationService;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTemplate<String, Object> redisObjectTemplate;
    private final UserDriverService userDriverService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final OSMGeocodingService geocodingService;




    private static final String BAN_CACHE_PREFIX = "banned:user:";
    private static final String USER_CACHE_PREFIX = "user:details:";
    private static final long BAN_CACHE_TTL_DAYS = 30;
    private static final long USER_CACHE_TTL_MINUTES = 60;

    @Value("${jwt.secret}")
    private String jwtSecret;


    @Override
    @Transactional
    public List<RideResponseDto> searchAvailableRides(String from, String to, LocalDate date) {
        return rideServiceClient.searchAvailableRides(from, to, date);
    }

    private String generateSecureToken(UUID requestId, UUID driverId) {
        return Jwts.builder()
                .setSubject("RequestApproval")
                .claim("requestId", requestId.toString())
                .claim("driverId", driverId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // expires in 5 mins
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    @Override
    @Transactional
    public Request createRequest(RequestDTO dto, UUID senderId) {
        checkUserBanned(senderId);
        UUID assignedDriverId = null;

        // ✅ 1. Resolve assigned driver (if applicable)
        if (dto.getRideId() != null) {
            try {
                RideResponseDto ride = rideServiceClient.getRideById(dto.getRideId());
                assignedDriverId = ride.getRideUserId(); // ✅ this exists in RideResponseDto
                checkUserBanned(assignedDriverId);
            } catch (Exception e) {
                log.error("Error fetching ride or driver details: {}", e.getMessage());
            }
        }

        // ✅ 2. Fetch coordinates from 'from' and 'to' locations
        double[] fromCoords = geocodingService.getCoordinatesFromLocation(dto.getFrom());
        double[] toCoords = geocodingService.getCoordinatesFromLocation(dto.getTo());

        // ✅ 3. Build and save request
        Request request = Request.builder()
                .senderUserId(senderId)
                .rideUserId(dto.getRideUserId())
                .rideId(dto.getRideId())
                .goodsDescription(dto.getGoodsDescription())
                .goodsType(dto.getGoodsType())
                .requiredSpace(dto.getRequiredSpace())
                .weight(dto.getWeight())
                .goodsQuantity(dto.getGoodsQuantity())
                .fare(dto.getFare())
                .from(dto.getFrom())
                .to(dto.getTo())
                .fromLatitude(fromCoords[0])
                .fromLongitude(fromCoords[1])
                .toLatitude(toCoords[0])
                .toLongitude(toCoords[1])
                .specialInstructions(dto.getSpecialInstructions())
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate())
                .build();

        Request saved = requestRepository.save(request);
        saveHistory(saved, RequestStatus.PENDING, "Request created");

        // ✅ 4. Trigger notifications
        notificationService.handleDriverAndUserNotifications(saved, dto);

        return saved;
    }




    @Override
    public Request getRequestById(UUID id, UUID requesterId) {
        checkUserBanned(requesterId);
        Request request = findRequest(id);
        if (!Objects.equals(request.getSenderUserId(), requesterId) &&
                !Objects.equals(request.getRideUserId(), requesterId)) {
            throw new RequestAccessDeniedException("You are not authorized to view this request");
        }
        return request;
    }

    @Override
    @Transactional
    public Request updateRequest(UUID id, RequestUpdateDTO dto, UUID requesterId) {
        checkUserBanned(requesterId);
        Request request = findRequest(id);
        if (!request.getSenderUserId().equals(requesterId)) {
            throw new UnauthorizedUserException("You can only update your own request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStatusException("Only pending requests can be updated");
        }

        request.setGoodsDescription(dto.getGoodsDescription());
        request.setWeight(dto.getWeight());

        request.setFrom(dto.getFrom());
        request.setTo(dto.getTo());
        request.setSpecialInstructions(dto.getSpecialInstructions());
        request.setUpdatedAt(LocalDateTime.now());
        request.setGoodsQuantity(dto.getGoodsQuantity());
        Request saved = requestRepository.save(request);
        saveHistory(saved, request.getStatus(), "Request updated");

        webSocketNotificationHandler.sendUserNotification(requesterId, id + ":Request updated successfully");
        if (request.getRideUserId() != null) {
            webSocketNotificationHandler.sendDriverNotification(request.getRideUserId(),
                    id + ":Request details updated by sender");
        }

        return saved;
    }

    @Override
    @Transactional
    public void cancelRequest(UUID id, UUID requesterId) {
        checkUserBanned(requesterId);
        Request request = findRequest(id);
        if (!request.getSenderUserId().equals(requesterId)) {
            throw new UnauthorizedUserException("Only the sender can cancel the request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStatusException("Only pending requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        saveHistory(request, RequestStatus.CANCELLED, "Cancelled by sender");

        webSocketNotificationHandler.sendUserNotification(requesterId, id + ":Request cancelled successfully");
        if (request.getRideUserId()!= null) {
            webSocketNotificationHandler.sendDriverNotification(request.getRideUserId(),
                    id + ":Request cancelled by sender");
        }
    }

    @Override
    public Page<Request> getRequestsByUser(UUID userId, Pageable pageable, RequestStatus status) {
        checkUserBanned(userId);
        return status != null ?
                requestRepository.findBySenderUserIdAndStatus(userId, status, pageable) :
                requestRepository.findBySenderUserId(userId, pageable);
    }

    @Override
    public RequestSummaryResponse getRequestsSummary(UUID userId) {
        checkUserBanned(userId);
        List<Request> requests = requestRepository.findBySenderUserId(userId); // ✅ fixed

        Map<RequestStatus, Long> countByStatus = requests.stream()
                .collect(Collectors.groupingBy(Request::getStatus, Collectors.counting()));

        return RequestSummaryResponse.builder()
                .totalRequests(requests.size())
                .pending(countByStatus.getOrDefault(RequestStatus.PENDING, 0L))
                .accepted(countByStatus.getOrDefault(RequestStatus.ACCEPTED, 0L))
                .inTransit(countByStatus.getOrDefault(RequestStatus.IN_TRANSIT, 0L))
                .delivered(countByStatus.getOrDefault(RequestStatus.DELIVERED, 0L))
                .cancelled(countByStatus.getOrDefault(RequestStatus.CANCELLED, 0L))
                .build();
    }


    @Override
    public List<Request> getRequestsForRide(UUID rideId) {
        return requestRepository.findByRideId(rideId);
    }

    @Override
    public Page<Request> getRequestsByRideUser(UUID rideUserId, Pageable pageable, RequestStatus status) {
        checkUserBanned(rideUserId);
        return status != null ?
                requestRepository.findByRideUserIdAndStatus(rideUserId, status, pageable) :
                requestRepository.findByRideUserId(rideUserId, pageable);
    }


    @Override
    public List<Request> getActiveRequestsForRideUser(UUID rideUserId) {
        checkUserBanned(rideUserId);
        log.info("Fetching active requests for driverId: {}", rideUserId);
        List<Request> requests = requestRepository.findByRideUserIdAndStatus(rideUserId, RequestStatus.IN_TRANSIT);
        log.info("Found {} active requests for driverId: {}", requests.size(), rideUserId);
        return requests;
    }

    @Override
    @Transactional
    public Request acceptRequest(UUID id, UUID driverId) {
        checkUserBanned(driverId);
        Request request = findRequest(id);

        if (!request.getRideUserId().equals(driverId)) {
            throw new UnauthorizedUserException("You are not the assigned driver for this request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStatusException("Only pending requests can be accepted");
        }

        request.setStatus(RequestStatus.ACCEPTED);
        request.setAcceptedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        Request saved = requestRepository.save(request);
        saveHistory(saved, RequestStatus.ACCEPTED, "Accepted by driver");

        webSocketNotificationHandler.sendUserNotification(saved.getSenderUserId(), id + ":Request accepted by driver");
        webSocketNotificationHandler.sendDriverNotification(driverId, id + ":Request accepted successfully");

        webSocketNotificationHandler.sendTrackingUpdate(
                saved.getSenderUserId(),
                driverId,
                id.toString(),
                "ACCEPTED",
                saved.getFrom()
        );

        return saved;
    }

    @Override
    @Transactional
    public Request rejectRequest(UUID id, UUID driverId, String reason) {
        checkUserBanned(driverId);
        Request request = findRequest(id);

        if (!Objects.equals(request.getRideUserId(), driverId)) {
            throw new UnauthorizedUserException("You can only reject requests assigned to you");
        }

        // If you want to make the request available again:
        request.setRideUserId(null);
        request.setStatus(RequestStatus.PENDING); // or REJECTED if you want to mark as rejected
        request.setUpdatedAt(LocalDateTime.now());

        Request saved = requestRepository.save(request);
        String rejectionNote = "Rejected by driver" + (reason != null ? ": " + reason : "");
        saveHistory(saved, RequestStatus.PENDING, rejectionNote);

        String rejectionMessage = id + ":Request rejected" + (reason != null ? ": " + reason : "");
        webSocketNotificationHandler.sendUserNotification(request.getSenderUserId(), rejectionMessage);
        webSocketNotificationHandler.sendDriverNotification(driverId, id + ":Request rejection confirmed");
        webSocketNotificationHandler.broadcastToAllDrivers(id + ":Request available again after rejection");

        return saved;
    }

    @Override
    @Transactional
    public Request markAsPickedUp(UUID id, UUID driverId) {
        checkUserBanned(driverId);
        Request request = findRequest(id);
        if (!driverId.equals(request.getRideUserId())) {
            throw new UnauthorizedUserException("Only assigned driver can mark as picked up");
        }
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new InvalidRequestStatusException("Only accepted requests can be marked as picked up");
        }

        request.setStatus(RequestStatus.IN_TRANSIT);
        request.setPickedUpAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        Request saved = requestRepository.save(request);
        saveHistory(saved, RequestStatus.IN_TRANSIT, "Goods picked up by driver");

        LocationDTO location = locationService.getDriverLocation(driverId);
        String locationStr = location != null ? location.toString() : request.getFrom();
        webSocketNotificationHandler.sendTrackingUpdate(request.getSenderUserId(), driverId, id.toString(), "IN_TRANSIT", locationStr);
        webSocketNotificationHandler.sendUserNotification(request.getSenderUserId(), id + ":Your goods have been picked up");
        webSocketNotificationHandler.sendDriverNotification(driverId, id + ":Pickup confirmed - goods in transit");

        return saved;
    }

    @Override
    @Transactional
    public Request markAsDelivered(UUID id, UUID driverId, String notes) {
        checkUserBanned(driverId);
        Request request = findRequest(id);
        if (!driverId.equals(request.getRideUserId())) {
            throw new UnauthorizedUserException("Only assigned driver can mark as delivered");
        }
        if (request.getStatus() != RequestStatus.IN_TRANSIT) {
            throw new InvalidRequestStatusException("Only in-transit requests can be marked as delivered");
        }

        request.setStatus(RequestStatus.DELIVERED);
        request.setDeliveredAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        Request saved = requestRepository.save(request);
        String deliveryNote = "Goods delivered successfully" + (notes != null ? ". Notes: " + notes : "");
        saveHistory(saved, RequestStatus.DELIVERED, deliveryNote);

        try {
            processPayment(saved);
        } catch (Exception e) {
            log.error("Payment processing failed for request {}: {}", id, e.getMessage());
        }

        LocationDTO location = locationService.getDriverLocation(driverId);
        String locationStr = location != null ? location.toString() : request.getTo();
        webSocketNotificationHandler.sendTrackingUpdate(request.getSenderUserId(), driverId, id.toString(), "DELIVERED", locationStr);
        webSocketNotificationHandler.sendUserNotification(request.getSenderUserId(), id + ":Goods delivered successfully");
        webSocketNotificationHandler.sendDriverNotification(driverId, id + ":Delivery confirmed - payment processed");

        return saved;
    }

    @Override
    public RequestStatusResponse getRequestStatus(UUID id, UUID userId) {
        checkUserBanned(userId);
        Request request = getRequestById(id, userId);
        return RequestStatusResponse.builder()
                .requestId(request.getId())
                .status(request.getStatus())
                .acceptedAt(request.getAcceptedAt())
                .pickedUpAt(request.getPickedUpAt())
                .deliveredAt(request.getDeliveredAt())
                .build();
    }

    @Override
    public Page<Request> getRequestHistory(UUID userId, Pageable pageable, String from, String to) {
        checkUserBanned(userId);
        if (from != null && to != null) {
            LocalDateTime fromDateTime = LocalDate.parse(from).atStartOfDay();
            LocalDateTime toDateTime = LocalDate.parse(to).atStartOfDay().plusDays(1);
            return requestRepository.findBySenderUserIdAndCreatedAtBetween(userId, fromDateTime, toDateTime, pageable);
        }
        return requestRepository.findBySenderUserId(userId, pageable);
    }

    @Override
    public Page<Request> getRideUserRequestHistory(UUID rideUserId, Pageable pageable, String from, String to, RequestStatus status) {
        checkUserBanned(rideUserId);
        if (from != null && to != null) {
            LocalDateTime fromDateTime = LocalDate.parse(from).atStartOfDay();
            LocalDateTime toDateTime = LocalDate.parse(to).atStartOfDay().plusDays(1);
            return requestRepository.findByRideUserIdAndStatusAndCreatedAtBetween(
                    rideUserId, status, fromDateTime, toDateTime, pageable);
        }
        return status != null ?
                requestRepository.findByRideUserIdAndStatus(rideUserId, status, pageable) :
                requestRepository.findByRideUserId(rideUserId, pageable);
    }

    @Override
    public Request getRequestDetailedHistory(UUID requestId, UUID userId) {
        checkUserBanned(userId);
        return getRequestById(requestId, userId);
    }

    @Override
    public Page<Request> getCompletedRequestsHistory(UUID userId, Pageable pageable) {
        checkUserBanned(userId);
        return requestRepository.findBySenderUserIdAndStatusIn(
                userId, List.of(RequestStatus.DELIVERED, RequestStatus.CANCELLED), pageable);
    }

    @Override
    public Page<Request> searchRequests(RequestFilterDTO dto, Pageable pageable, UUID userId) {
        checkUserBanned(userId);
        log.info("Searching requests with filters: pickupLocation={}, deliveryLocation={}, status={}",
                dto.getFrom(), dto.getTo(), dto.getStatus());
        Page<Request> result = requestRepository.findByFromAndTo(
                dto.getFrom(),
                dto.getTo(),

                dto.getStatus(),
                pageable
        );
        log.info("Found {} requests matching filters", result.getTotalElements());
        return result;
    }

    @Override
    public List<Request> getRequestsByLocation(String pickup, String delivery, int radius, UUID userId) {
        checkUserBanned(userId);
        return requestRepository.findByFromAndTo(pickup, delivery);
    }

    @Override
    public Page<Request> getAllRequests(Pageable pageable, RequestStatus status) {
        return status != null ?
                requestRepository.findByStatus(status, pageable) :
                requestRepository.findAll(pageable);
    }

    @Override
    public List<Request> getRequestsByUserAdmin(UUID userId) {
        checkUserBanned(userId);
        return requestRepository.findBySenderUserId(userId);
    }

    @Override
    @Transactional
    public void updateRideUserLocation(UUID rideUserId, LocationDTO location) {
        checkUserBanned(rideUserId);
        locationService.updateDriverLocation(rideUserId, location);
        log.info("Driver location updated for driverId: {}", rideUserId);
    }

    @Override
    @Transactional
    public void updateRequestTracking(UUID requestId, LocationDTO location, UUID driverId) {
        checkUserBanned(driverId);
        Request request = findRequest(requestId);
        if (!driverId.equals(request.getRideUserId())) {
            throw new UnauthorizedUserException("Only assigned driver can update tracking");
        }
        locationService.updateRequestTracking(requestId, location);
        webSocketNotificationHandler.sendTrackingUpdate(request.getSenderUserId(), driverId, requestId.toString(),
                request.getStatus().toString(), location.toString());
        log.info("Tracking updated for requestId: {}", requestId);
    }

    @Override
    public LocationDTO getRequestTracking(UUID requestId, UUID userId) {
        checkUserBanned(userId);
        Request request = findRequest(requestId);
        if (!Objects.equals(request.getSenderUserId(), userId) &&
                !Objects.equals(request.getRideUserId(), userId)) {
            throw new RequestAccessDeniedException("You are not authorized to view tracking");
        }
        return locationService.getRequestTracking(requestId);
    }

    @Override
    @Transactional
    public void banUser(UUID userId, String reason) {
        try {
            userServiceClient.getUserDetails(userId);
        } catch (Exception e) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        String cacheKey = BAN_CACHE_PREFIX + userId;
        redisStringTemplate.opsForValue().set(cacheKey, reason, BAN_CACHE_TTL_DAYS, TimeUnit.DAYS);

        webSocketNotificationHandler.sendUserNotification(userId, "Your account has been banned. Reason: " + reason);
        log.info("User {} banned with reason: {}", userId, reason);
    }

    @Override
    public TransactionReport generateTransactionReport(String fromDate, String toDate) {
        LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
        LocalDateTime to = LocalDate.parse(toDate).atStartOfDay().plusDays(1);
        List<Request> requests = requestRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(from) && r.getCreatedAt().isBefore(to))
                .filter(r -> r.getStatus() == RequestStatus.DELIVERED)
                .collect(Collectors.toList());

        BigDecimal totalAmount = requests.stream()
                .map(Request::getFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commission = totalAmount.multiply(new BigDecimal("0.05"));

        return TransactionReport.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalTransactions(requests.size())
                .totalAmount(totalAmount)
                .commissionEarned(commission)
                .build();
    }

    private void processPayment(Request request) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                PaymentRequestDto paymentRequest = new PaymentRequestDto();
                paymentRequest.setUserId(request.getSenderUserId());
                paymentRequest.setRequestId(request.getId());
                BigDecimal commissionRate = new BigDecimal("0.05");
                paymentRequest.setAmount(request.getFare().multiply(BigDecimal.ONE.subtract(commissionRate)));
                paymentRequest.setPaymentMethod("UPI");

                paymentServiceClient.processPayment(paymentRequest);
                PaymentStatusResponseDto status = paymentServiceClient.getPaymentStatus(request.getId());

                if ("SUCCESS".equals(status.getStatus())) {
                    log.info("Payment processed successfully for request {}", request.getId());
                    webSocketNotificationHandler.sendUserNotification(request.getSenderUserId(),
                            request.getId() + ":Payment processed successfully");
                    return;
                } else {
                    log.error("Payment failed for request {}: {}", request.getId(), status.getMessage());
                }
            } catch (Exception e) {
                log.error("Payment attempt {} failed for request {}: {}", retryCount + 1, request.getId(), e.getMessage());
            }
            retryCount++;
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Payment processing failed after " + maxRetries + " attempts");
    }

    private Request findRequest(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + id));
    }

    private void saveHistory(Request request, RequestStatus status, String notes) {
        RequestHistory history = RequestHistory.builder()
                .request(request)
                .status(status)
                .statusTimestamp(LocalDateTime.now())
                .notes(notes)
                .build();
        requestHistoryRepository.save(history);
    }

    private void checkUserBanned(UUID userId) {
        String cacheKey = BAN_CACHE_PREFIX + userId;
        String banReason = redisStringTemplate.opsForValue().get(cacheKey);
        if (banReason != null) {
            throw new UnauthorizedUserException("User is banned: " + banReason);
        }
    }

    private UserResponseDTO getCachedUserDetails(UUID userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;

        // 🔁 Try to fetch from Redis cache
        UserResponseDTO cached = (UserResponseDTO) redisObjectTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 🔁 Fetch from user service and cache it
        UserResponseDTO user = userServiceClient.getUserDetails(userId).getData(); // ✅ FIXED HERE
        redisObjectTemplate.opsForValue().set(cacheKey, user, USER_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return user;
    }

}