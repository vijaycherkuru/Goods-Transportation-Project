package com.gdc.requests_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.request.LocationDTO;
import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.service.RequestService;
import com.gdc.requests_management.websocket.WebSocketNotificationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private WebSocketNotificationHandler webSocketNotificationHandler;

    @MockBean
    private UserServiceClient userServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID requestId;
    private Request request;
    private RequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        requestDTO = new RequestDTO();
        requestDTO.setSenderId(userId);
        requestDTO.setGoodsDescription("Books");
        requestDTO.setWeight(5.0);
        requestDTO.setVolume(1.0);
        requestDTO.setPickupLocation("Village A");
        requestDTO.setDeliveryLocation("City B");
        requestDTO.setEstimatedValue(new BigDecimal("100.00"));
        requestDTO.setPriority(RequestPriority.MEDIUM);

        request = Request.builder()
                .id(requestId)
                .senderId(userId)
                .goodsDescription("Books")
                .weight(5.0)
                .volume(1.0)
                .pickupLocation("Village A")
                .deliveryLocation("City B")
                .estimatedValue(new BigDecimal("100.00"))
                .status(RequestStatus.PENDING)
                .priority(RequestPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "user-id", roles = {"USER"})
    void createRequest_Success() throws Exception {
        when(requestService.createRequest(any(RequestDTO.class), eq(userId)))
                .thenReturn(request);

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(requestId.toString()))
                .andExpect(jsonPath("$.message").value("Transport request created successfully"));
    }

    @Test
    @WithMockUser(username = "user-id", roles = {"USER"})
    void getRequestById_Success() throws Exception {
        when(requestService.getRequestById(eq(requestId), eq(userId)))
                .thenReturn(request);

        mockMvc.perform(post("/api/v1/requests/" + requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId.toString()))
                .andExpect(jsonPath("$.message").value("Request retrieved successfully"));
    }

    @Test
    @WithMockUser(username = "driver-id", roles = {"DRIVER"})
    void acceptRequest_Success() throws Exception {
        request.setStatus(RequestStatus.ACCEPTED);
        when(requestService.acceptRequest(eq(requestId), any(UUID.class)))
                .thenReturn(request);

        mockMvc.perform(put("/api/v1/requests/" + requestId + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.message").value("Request accepted successfully"));
    }

    @Test
    @WithMockUser(username = "driver-id", roles = {"DRIVER"})
    void updateRequestTracking_Success() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLatitude(12.34);
        locationDTO.setLongitude(56.78);

        mockMvc.perform(put("/api/v1/requests/" + requestId + "/tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tracking updated successfully"));
    }
}