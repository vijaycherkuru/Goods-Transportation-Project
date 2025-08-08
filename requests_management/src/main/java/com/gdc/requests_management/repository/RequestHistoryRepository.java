package com.gdc.requests_management.repository;

import com.gdc.requests_management.model.entity.RequestHistory;
import com.gdc.requests_management.model.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, UUID> {

    // ✅ Corrected method: fetch history for a request ordered by time
    List<RequestHistory> findByRequestOrderByStatusTimestampAsc(Request request);
}
