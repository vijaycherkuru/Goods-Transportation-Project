package com.gdc.requests_management.scheduler;

import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.repository.RequestRepository;
import com.gdc.requests_management.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAutoRejectScheduler {

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 5 minutes to auto-reject PENDING requests older than 15 minutes (created within last 2 hours).
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void autoRejectPendingRequests() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusMinutes(15);      // reject if older than 15 min
        LocalDateTime recentWindow = now.minusHours(2);       // ignore anything older than 2 hrs

        List<Request> stalePendingRequests = requestRepository
                .findByStatusAndCreatedAtBefore(RequestStatus.PENDING, cutoffTime)
                .stream()
                .filter(request -> request.getCreatedAt().isAfter(recentWindow))
                .collect(Collectors.toList());

        for (Request request : stalePendingRequests) {
            try {
                request.setStatus(RequestStatus.REJECTED);
                request.setUpdatedAt(LocalDateTime.now());
                requestRepository.save(request);

                notificationService.notifyUserAutoRejected(request);

                log.info("🛑 Auto-rejected request {} due to driver timeout", request.getId());
            } catch (Exception ex) {
                log.error("❌ Failed to auto-reject request {}: {}", request.getId(), ex.getMessage(), ex);
            }
        }
    }
}
