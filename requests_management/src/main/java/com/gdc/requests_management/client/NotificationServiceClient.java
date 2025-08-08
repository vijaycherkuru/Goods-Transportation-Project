package com.gdc.requests_management.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface
NotificationServiceClient {
    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody Map<String, Object> payload);

}