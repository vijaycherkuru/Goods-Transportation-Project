package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.ResetPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.ForgotPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.ResetPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.PasswordResetFormResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.VerifyOtpResponseDTO;
import com.gdc.user_registration_and_authentication.entity.ForgotPassword;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.exception.UserNotFoundException;
import com.gdc.user_registration_and_authentication.repository.ForgotPasswordRepository;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ForgotPasswordResponseDTO sendOtpToEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

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
                    .user(user)
                    .otp(otp)
                    .expirationtime(calendar.getTime())
                    .build();

            forgotPasswordRepository.save(forgotPassword);

            // Send OTP email
            emailService.sendOtpEmail(email, String.valueOf(otp));

            log.info("OTP sent successfully to email: {}", email);

            return ForgotPasswordResponseDTO.builder()
                    .success(true)
                    .message("OTP has been sent to your email address")
                    .email(email)
                    .otpExpiryMinutes(10)
                    .build();

        } catch (UserNotFoundException e) {
            log.error("User not found: {}", email);
            return ForgotPasswordResponseDTO.builder()
                    .success(false)
                    .message("User with this email does not exist")
                    .build();
        } catch (Exception e) {
            log.error("Error sending OTP to email: {}", email, e);
            return ForgotPasswordResponseDTO.builder()
                    .success(false)
                    .message("Failed to send OTP. Please try again later.")
                    .build();
        }
    }

    @Override
    public VerifyOtpResponseDTO verifyOtp(String email, String otpString) {
        try {
            int otp = Integer.parseInt(otpString);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            ForgotPassword forgotPassword = forgotPasswordRepository.findByUser(user)
                    .orElse(null);

            if (forgotPassword == null) {
                return VerifyOtpResponseDTO.builder()
                        .success(false)
                        .message("No OTP request found. Please request a new OTP.")
                        .build();
            }

            // Check if OTP matches
            if (forgotPassword.getOtp() != otp) {
                return VerifyOtpResponseDTO.builder()
                        .success(false)
                        .message("Invalid OTP. Please try again.")
                        .build();
            }

            // Check if OTP is expired
            if (forgotPassword.getExpirationtime().before(new Date())) {
                forgotPasswordRepository.delete(forgotPassword);
                return VerifyOtpResponseDTO.builder()
                        .success(false)
                        .message("OTP has expired. Please request a new OTP.")
                        .build();
            }

            log.info("OTP verified successfully for email: {}", email);

            // Return only verification status - no sensitive data
            return VerifyOtpResponseDTO.builder()
                    .success(true)
                    .message("OTP verified successfully")
                    .build();

        } catch (NumberFormatException e) {
            return VerifyOtpResponseDTO.builder()
                    .success(false)
                    .message("Invalid OTP format")
                    .build();
        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}", email, e);
            return VerifyOtpResponseDTO.builder()
                    .success(false)
                    .message("Failed to verify OTP. Please try again.")
                    .build();
        }
    }

    @Override
    public PasswordResetFormResponseDTO getPasswordResetForm(String email, String otpString) {
        try {
            // Verify OTP first
            VerifyOtpResponseDTO otpVerification = verifyOtp(email, otpString);
            if (!otpVerification.isSuccess()) {
                return PasswordResetFormResponseDTO.builder()
                        .success(false)
                        .message(otpVerification.getMessage())
                        .showPasswordForm(false)
                        .build();
            }

            // If OTP is valid, show password reset form
            return PasswordResetFormResponseDTO.builder()
                    .success(true)
                    .message("OTP verified. Please enter your new password.")
                    .showPasswordForm(true)
                    .email(email)
                    .otp(otpString)
                    .passwordRequirements(java.util.Arrays.asList(
                            "Password must be at least 8 characters long",
                            "Include uppercase and lowercase letters",
                            "Include at least one number",
                            "Include at least one special character"
                    ))
                    .build();

        } catch (Exception e) {
            log.error("Error getting password reset form for email: {}", email, e);
            return PasswordResetFormResponseDTO.builder()
                    .success(false)
                    .message("Failed to load password reset form. Please try again.")
                    .showPasswordForm(false)
                    .build();
        }
    }

    @Override
    public ResetPasswordResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        try {
            // First verify OTP again for security
            VerifyOtpResponseDTO otpVerification = verifyOtp(request.getEmail(), request.getOtp());
            if (!otpVerification.isSuccess()) {
                return ResetPasswordResponseDTO.builder()
                        .success(false)
                        .message(otpVerification.getMessage())
                        .build();
            }

            // Check if passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResetPasswordResponseDTO.builder()
                        .success(false)
                        .message("Passwords do not match")
                        .build();
            }

            // Validate password strength
            if (request.getNewPassword().length() < 8) {
                return ResetPasswordResponseDTO.builder()
                        .success(false)
                        .message("Password must be at least 8 characters long")
                        .build();
            }

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Delete the OTP record as it's been used
            forgotPasswordRepository.findByUser(user)
                    .ifPresent(forgotPasswordRepository::delete);

            log.info("Password reset successfully for email: {}", request.getEmail());

            return ResetPasswordResponseDTO.builder()
                    .success(true)
                    .message("Password has been reset successfully")
                    .email(request.getEmail())
                    .redirectUrl("/login") // Frontend can use this to redirect
                    .build();

        } catch (Exception e) {
            log.error("Error resetting password for email: {}", request.getEmail(), e);
            return ResetPasswordResponseDTO.builder()
                    .success(false)
                    .message("Failed to reset password. Please try again.")
                    .build();
        }
    }

    @Override
    public ForgotPasswordResponseDTO resendOtp(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Delete existing OTP
            forgotPasswordRepository.findByUser(user)
                    .ifPresent(forgotPasswordRepository::delete);

            // Send new OTP
            return sendOtpToEmail(email);

        } catch (Exception e) {
            log.error("Error resending OTP to email: {}", email, e);
            return ForgotPasswordResponseDTO.builder()
                    .success(false)
                    .message("Failed to resend OTP. Please try again later.")
                    .build();
        }
    }
}
