package com.gdc.requests_management.service.impl;

import com.gdc.requests_management.client.PaymentServiceClient;
import com.gdc.requests_management.client.RideServiceClient;
import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.request.LocationDTO;
import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.dto.response.RequestSummaryResponse;
import com.gdc.requests_management.exception.*;
import com.gdc.requests_management.feign.dto.RideRequestResponseDto;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.repository.RequestHistoryRepository;
import com.gdc.requests_management.repository.RequestRepository;
import com.gdc.requests_management.service.LocationService;
import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestHistoryRepository requestHistoryRepository;

    @Mock
    private WebSocketNotificationHandler webSocketNotificationHandler;

    @Mock
    private RideServiceClient rideServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private LocationService locationService;

    @Mock
    private RedisTemplate<String, String> redisStringTemplate;

    @Mock
    private RedisTemplate<String, Object> redisObjectTemplate;

    @InjectMocks
    private RequestServiceImpl requestService;

    private UUID userId;
    private UUID driverId;
    private UUID requestId;
    private RequestDTO requestDTO;
    private Request request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        requestDTO = new RequestDTO();
        requestDTO.setSenderUserId((userId));
        requestDTO.setGoodsDescription("Books");
        requestDTO.setWeight(5.0);
        requestDTO.setVolume(1.0);
        requestDTO.setFrom("Village A");
        requestDTO.setTo("City B");
        requestDTO.setFare(new BigDecimal("100.00"));


        request = Request.builder()
                .id(requestId)
                .senderUserId(userId)
                .goodsDescription("Books")
                .weight(5.0)
                .volume(1.0)
                .from("Village A")
                .to"City B")
                .fare(new BigDecimal("100.00"))
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_Success() {
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.save(any(Request.class))).thenReturn(request);
        when(rideServiceClient.getRideById(any(UUID.class))).thenReturn(new RideRequestResponseDto(ResponseDTO());

        Request result = requestService.createRequest(requestDTO, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(requestRepository, times(1)).save(any(Request.class));
        verify(webSocketNotificationHandler, times(1)).sendUserNotification(eq(userId), anyString());
    }

    @Test
    void createRequest_UserBanned_ThrowsException() {
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn("Banned for violation");

        assertThrows(UnauthorizedUserException.class, () -> requestService.createRequest(requestDTO, userId));
    }

    @Test
    void getRequestById_Success() {
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Request result = requestService.getRequestById(requestId, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(requestRepository, times(1)).findById(requestId);
    }

    @Test
    void getRequestById_Unauthorized_ThrowsException() {
        Request unauthorizedRequest = Request.builder()
                .id(requestId)
                .senderId(UUID.randomUUID())
                .status(RequestStatus.PENDING)
                .build();
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(unauthorizedRequest));

        assertThrows(RequestAccessDeniedException.class, () -> requestService.getRequestById(requestId, userId));
    }

    @Test
    void acceptRequest_Success() {
        request.setAssignedDriverId(driverId);
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        Request result = requestService.acceptRequest(requestId, driverId);

        assertNotNull(result);
        assertEquals(RequestStatus.ACCEPTED, result.getStatus());
        verify(webSocketNotificationHandler, times(1)).sendDriverNotification(eq(driverId), anyString());
        verify(webSocketNotificationHandler, times(1)).sendUserNotification(eq(userId), anyString());
    }

    @Test
    void markAsDelivered_Success() {
        request.setAssignedDriverId(driverId);
        request.setStatus(RequestStatus.IN_TRANSIT);
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        Request result = requestService.markAsDelivered(requestId, driverId, "Delivered on time");

        assertNotNull(result);
        assertEquals(RequestStatus.DELIVERED, result.getStatus());
        verify(webSocketNotificationHandler, times(1)).sendTrackingUpdate(eq(userId), eq(driverId), eq(requestId.toString()), eq("DELIVERED"), anyString());
    }

    @Test
    void updateRequestTracking_Success() {
        request.setAssignedDriverId(driverId);
        LocationDTO location = new LocationDTO();
        location.setLatitude(12.34);
        location.setLongitude(56.78);
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        requestService.updateRequestTracking(requestId, location, driverId);

        verify(locationService, times(1)).updateRequestTracking(eq(requestId), eq(location));
        verify(webSocketNotificationHandler, times(1)).sendTrackingUpdate(eq(userId), eq(driverId), eq(requestId.toString()), anyString(), anyString());
    }

    @Test
    void getRequestTracking_Success() {
        request.setAssignedDriverId(driverId);
        LocationDTO location = new LocationDTO();
        location.setLatitude(12.34);
        location.setLongitude(56.78);
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(locationService.getRequestTracking(requestId)).thenReturn(location);

        LocationDTO result = requestService.getRequestTracking(requestId, driverId);

        assertNotNull(result);
        assertEquals(12.34, result.getLatitude());
        verify(locationService, times(1)).getRequestTracking(requestId);
    }

    @Test
    void getRequestsSummary_Success() {
        when(redisStringTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(requestRepository.findBySenderId(userId)).thenReturn(List.of(request));

        RequestSummaryResponse summary = requestService.getRequestsSummary(userId);

        assertNotNull(summary);
        assertEquals(1, summary.getTotalRequests());
        assertEquals(1, summary.getPending());
    }
}