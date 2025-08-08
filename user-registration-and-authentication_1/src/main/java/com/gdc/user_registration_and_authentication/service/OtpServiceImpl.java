package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.OtpRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.OtpVerificationRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpVerificationResponseDTO;
import com.gdc.user_registration_and_authentication.entity.Otp;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.exception.UserNotFoundException;
import com.gdc.user_registration_and_authentication.repository.OtpRepository;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import com.gdc.user_registration_and_authentication.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${app.debug}")
    private boolean debugMode;

    @Override
    public OtpResponseDTO sendOtp(OtpRequestDTO requestDTO) {
        Optional<User> userOptional = Optional.empty();

        if (requestDTO.getEmail() != null) {
            userOptional = userRepository.findByEmail(requestDTO.getEmail());
        } else if (requestDTO.getPhone() != null) {
            userOptional = userRepository.findByPhone(requestDTO.getPhone());
        }

        User user = userOptional.orElseThrow(() ->
                new UserNotFoundException("User not found with given email or mobile number."));

        String otpCode = generateOtp(); // now numeric only
        Instant now = Instant.now();

        Otp otp = Otp.builder()
                .user(user)
                .otp(otpCode)
                .otpType(requestDTO.getOtpType())
                .createdAt(Timestamp.from(now))
                .expiresAt(Timestamp.from(now.plus(5, ChronoUnit.MINUTES)))
                .build();

        otpRepository.save(otp);

        log.info("OTP sent to user: {} | Type: {} | OTP: {}", user.getId(), requestDTO.getOtpType(), otpCode);

        if (user.getEmail() != null) {
            emailService.sendOtpEmail(user.getEmail(), otpCode);
        }

        OtpResponseDTO.OtpResponseDTOBuilder responseBuilder = OtpResponseDTO.builder()
                .userId(String.valueOf(user.getId()))
                .message("OTP sent successfully.");

        if (debugMode) {
            responseBuilder.otp(otpCode); // Only show OTP in debug mode
        }

        return responseBuilder.build();
    }

    private String generateOtp() {
        return OtpGenerator.generateOtp(6); // ✅ 6-digit numeric OTP
    }

    @Override
    public OtpVerificationResponseDTO verifyOtp(OtpVerificationRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        Otp latestOtp = otpRepository.findTopByUserAndOtpTypeOrderByCreatedAtDesc(user, requestDTO.getOtpType())
                .orElseThrow(() -> new RuntimeException("No OTP found for verification."));

        if (!latestOtp.getOtp().equals(requestDTO.getOtp())) {
            return OtpVerificationResponseDTO.builder()
                    .message("Invalid OTP")
                    .build();
        }

        if (latestOtp.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return OtpVerificationResponseDTO.builder()
                    .message("OTP has expired")
                    .build();
        }

        if ("EMAIL_VERIFICATION".equalsIgnoreCase(requestDTO.getOtpType().name())) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        return OtpVerificationResponseDTO.builder()
                .message("OTP verified successfully" +
                        (user.isEmailVerified() ? " and email marked as verified." : "."))
                .build();
    }
}
