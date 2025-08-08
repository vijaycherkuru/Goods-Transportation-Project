package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.UpdateProfileRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.UserRegistrationRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.UserProfileResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.UserRegistrationResponseDTO;
import com.gdc.user_registration_and_authentication.entity.User;

import java.util.UUID;

public interface UserService {
   // UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO requestDTO);
    User updateProfile(UUID userId, UpdateProfileRequestDTO requestDTO);
    UserProfileResponseDTO getUserProfile(UUID userId);
    User getUserById(UUID userId);
    String requestOtp(String email);
    UserRegistrationResponseDTO completeRegistration(UserRegistrationRequestDTO requestDTO);
    void verifyOtp(String email, String otp);
    //void verifyUserEmail(UUID userId, String otp);

}

