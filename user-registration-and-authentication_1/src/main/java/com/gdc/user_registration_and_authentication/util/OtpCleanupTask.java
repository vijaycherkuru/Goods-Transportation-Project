package com.gdc.user_registration_and_authentication.util;

import com.gdc.user_registration_and_authentication.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;

@Component
@RequiredArgsConstructor
public class OtpCleanupTask {

    private final OtpRepository otpRepository;

    @Scheduled(fixedRate = 300000) // Every 1 hour
    public void cleanExpiredOtps() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int deleted = otpRepository.deleteByExpiresAtBefore(now);
        System.out.println("🧹 Expired OTPs cleaned: " + deleted);
    }
}

