package com.gdc.user_registration_and_authentication.repository;

import com.gdc.user_registration_and_authentication.entity.Otp;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findTopByUserAndOtpTypeOrderByCreatedAtDesc(User user, OtpType otpType);
    Optional<Otp> findTopByUserOrderByCreatedAtDesc(User user);
    void deleteAllByUser(User user);
    int deleteByExpiresAtBefore(Timestamp now);

}
