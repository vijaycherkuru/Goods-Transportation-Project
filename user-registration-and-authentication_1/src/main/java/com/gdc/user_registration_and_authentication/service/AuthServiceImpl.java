package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.ForgotPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.LoginRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.ResetPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.ForgotPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.LoginResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.ResetPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.entity.ForgotPassword;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.exception.InvalidCredentialsException;
import com.gdc.user_registration_and_authentication.exception.UserNotFoundException;
import com.gdc.user_registration_and_authentication.repository.ForgotPasswordRepository;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import com.gdc.user_registration_and_authentication.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        String input = request.getEmailOrPhone().trim().toLowerCase(); // Normalize input
        Optional<User> optionalUser;

        // Lookup by email or phone or username
        if (input.contains("@")) {
            optionalUser = userRepository.findByEmailIgnoreCase(input);
        } else {
            optionalUser = userRepository.findByPhone(input);
        }

        User user = optionalUser.orElseThrow(() -> {
            log.warn("Login failed - user not found: {}", input);
            throw new InvalidCredentialsException("Invalid email/phone or password");
        });

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", input);
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Generate JWT
        String userId = user.getId().toString();
        String email = user.getEmail();
        String jwtToken = jwtUtil.generateToken(userId, email);


        log.info("Login successful for user: {}", email);

        // ✅ Return wrapped response (Standard JSON format)
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .message("Login Successful")
                .email(email)
                .username(user.getUsername())
                .userId(userId)
                .build();
    }


    @Override
    public ForgotPasswordResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        log.info("Forgot password request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new UserNotFoundException("User with email " + request.getEmail() + " not found");
                });

        // Generate 6-digit OTP
        int otp = 100000 + new Random().nextInt(900000);

        // Set expiration time (10 minutes from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);

        // Delete existing OTP if present
        forgotPasswordRepository.findByUser(user)
                .ifPresent(forgotPasswordRepository::delete);

        // Create new OTP record
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .fpid(UUID.randomUUID())
                .user(user)
                .otp(otp)
                .expirationtime(calendar.getTime())
                .build();

        forgotPasswordRepository.save(forgotPassword);

        // Send OTP email
        emailService.sendOtpEmail(request.getEmail(), String.valueOf(otp));

        log.info("OTP sent successfully to email: {}", request.getEmail());

        return ForgotPasswordResponseDTO.builder()
                .success(true)
                .message("OTP has been sent to your email address")
                .email(request.getEmail())
                .otpExpiryMinutes(10)
                .build();
    }

    @Override
    public ResetPasswordResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        log.info("Reset password request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new UserNotFoundException("User not found");
                });

        Optional<ForgotPassword> forgotPasswordOpt = forgotPasswordRepository.findByUser(user);

        if (forgotPasswordOpt.isEmpty()) {
            log.warn("No OTP found for user: {}", request.getEmail());
            throw new RuntimeException("No OTP request found. Please request a new OTP.");
        }

        ForgotPassword forgotPassword = forgotPasswordOpt.get();

        // Check if OTP matches
        if (forgotPassword.getOtp() != Integer.parseInt(request.getOtp())) {
            log.warn("Invalid OTP for user: {}", request.getEmail());
            throw new RuntimeException("Invalid OTP.");
        }

        // Check if OTP is expired
        if (forgotPassword.getExpirationtime().before(new Date())) {
            forgotPasswordRepository.delete(forgotPassword);
            log.warn("OTP expired for user: {}", request.getEmail());
            throw new RuntimeException("OTP has expired.");
        }

        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Passwords do not match for user: {}", request.getEmail());
            throw new RuntimeException("Passwords do not match");
        }

        // Validate password strength
        if (request.getNewPassword().length() < 8) {
            log.warn("Password too short for user: {}", request.getEmail());
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete the OTP record
        forgotPasswordRepository.delete(forgotPassword);

        log.info("Password reset successful");

        return ResetPasswordResponseDTO.builder()
                .success(true)
                .message("Password has been reset successfully")
                .email(request.getEmail())
                .redirectUrl("/login")
                .build();
    }
}