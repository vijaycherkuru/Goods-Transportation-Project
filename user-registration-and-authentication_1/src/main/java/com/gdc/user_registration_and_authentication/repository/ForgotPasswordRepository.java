package com.gdc.user_registration_and_authentication.repository;

import com.gdc.user_registration_and_authentication.entity.ForgotPassword;
import com.gdc.user_registration_and_authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, UUID> {
    Optional<ForgotPassword> findByUser(User user);
    Optional<ForgotPassword> findByOtp(int otp);
}
