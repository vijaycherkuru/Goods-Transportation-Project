package com.gdc.user_registration_and_authentication.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
    void sendEmail(String to, String subject, String body);
    void sendEmailVerificationOtp(String to, String otp); // <-- Add this line
}
