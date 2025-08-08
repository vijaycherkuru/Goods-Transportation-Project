package com.gdc.requests_management.service;

import com.gdc.requests_management.dto.request.RequestDTO;
import com.gdc.requests_management.model.entity.Request;

public interface NotificationService {

    /**
     * Handles notification logic for both driver and user after a request is created.
     *
     * @param request the request entity that was created
     * @param dto     the original DTO used to create the request (contains details like estimatedValue, from, to)
     */
    void handleDriverAndUserNotifications(Request request, RequestDTO dto);

    /**
     * Sends user notification when a request is auto-rejected (e.g., timeout).
     *
     * @param request the request that was auto-rejected
     */
    void notifyUserAutoRejected(Request request);
}
