package com.gdc.user_registration_and_authentication.scheduler;

import com.gdc.user_registration_and_authentication.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final OtpRepository otpRepository;

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void cleanExpiredOtps() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int deletedCount = otpRepository.deleteByExpiresAtBefore(now);
        log.info("🧹 Cleaned {} expired OTP(s) at {}", deletedCount, now);
    }
}
