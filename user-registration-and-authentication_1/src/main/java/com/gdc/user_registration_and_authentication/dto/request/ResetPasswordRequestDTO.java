package com.gdc.user_registration_and_authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}