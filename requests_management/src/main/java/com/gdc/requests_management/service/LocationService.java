package com.gdc.requests_management.service;

import com.gdc.requests_management.dto.request.LocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LocationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void updateDriverLocation(UUID rideUserId, LocationDTO location) {
        String key = "driver:location:" + rideUserId;
        redisTemplate.opsForValue().set(key, location, 1, TimeUnit.HOURS);
    }

    public LocationDTO getDriverLocation(UUID rideUserId) {
        return (LocationDTO) redisTemplate.opsForValue().get("driver:location:" + rideUserId);
    }

    public void updateRequestTracking(UUID requestId, LocationDTO location) {
        String key = "request:tracking:" + requestId;
        redisTemplate.opsForValue().set(key, location, 1, TimeUnit.HOURS);
    }

    public LocationDTO getRequestTracking(UUID requestId) {
        return (LocationDTO) redisTemplate.opsForValue().get("request:tracking:" + requestId);
    }
}