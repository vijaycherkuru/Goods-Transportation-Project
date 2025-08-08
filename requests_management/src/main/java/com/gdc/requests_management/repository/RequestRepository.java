package com.gdc.requests_management.repository;

import com.gdc.requests_management.model.entity.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {

    List<Request> findBySenderUserId(UUID senderUserId);
    Page<Request> findBySenderUserId(UUID senderUserId, Pageable pageable);
    Page<Request> findBySenderUserIdAndStatus(UUID senderUserId, RequestStatus status, Pageable pageable);

    List<Request> findByRideId(UUID rideId);
    Page<Request> findByRideUserId(UUID rideUserId, Pageable pageable);
    Page<Request> findByRideUserIdAndStatus(UUID rideUserId, RequestStatus status, Pageable pageable);
    List<Request> findByRideUserIdAndStatus(UUID rideUserId, RequestStatus status);

    Page<Request> findBySenderUserIdAndStatusIn(UUID senderUserId, List<RequestStatus> statuses, Pageable pageable);
    Page<Request> findByStatus(RequestStatus status, Pageable pageable);

    Page<Request> findByFromAndTo(
            String from,
            String to,
            RequestStatus status,
            Pageable pageable
    );

    List<Request> findByStatusAndCreatedAtBefore(RequestStatus status, LocalDateTime time);

    List<Request> findByFromAndTo(String from, String to);

    // ✅ Added methods for date filtering
    Page<Request> findBySenderUserIdAndCreatedAtBetween(UUID senderUserId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Request> findByRideUserIdAndStatusAndCreatedAtBetween(UUID rideUserId, RequestStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);
}