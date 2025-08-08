package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.UpdateProfileRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.UserRegistrationRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.UserProfileResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.UserRegistrationResponseDTO;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.exception.UserNotFoundException;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import com.gdc.user_registration_and_authentication.temp.TempUser;
import com.gdc.user_registration_and_authentication.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    private final Map<String, TempUser> otpStorage = new HashMap<>();

    @Override
    public String requestOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        String otp = OtpGenerator.generateOtp(6);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        otpStorage.put(email, TempUser.builder()
                .email(email)
                .otp(otp)
                .otpGeneratedAt(now)
                .otpVerified(false)
                .build()
        );

        // ✅ Send professional HTML email with OTP
        emailService.sendEmailVerificationOtp(email, otp);

        return "OTP sent successfully to " + email;
    }

    @Override
    public void verifyOtp(String email, String otp) {
        TempUser tempUser = otpStorage.get(email);
        if (tempUser == null) throw new RuntimeException("No OTP requested for this email");

        if (!tempUser.getOtp().equals(otp)) throw new RuntimeException("Invalid OTP");

        long now = System.currentTimeMillis();
        if ((now - tempUser.getOtpGeneratedAt().getTime()) > 5 * 60 * 1000) // 5 minutes
            throw new RuntimeException("OTP expired");

        tempUser.setOtpVerified(true);
    }

    @Override
    public UserRegistrationResponseDTO completeRegistration(UserRegistrationRequestDTO requestDTO) {
        TempUser tempUser = otpStorage.get(requestDTO.getEmail());
        if (tempUser == null || !tempUser.isOtpVerified())
            throw new RuntimeException("OTP verification required before registration");

        User user = User.builder()
                .username(requestDTO.getName())
                .email(tempUser.getEmail())
                .phone(requestDTO.getPhone())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .age(requestDTO.getAge())
                .gender(requestDTO.getGender())
                .street(requestDTO.getStreet())
                .city(requestDTO.getCity())
                .state(requestDTO.getState())
                .zipCode(requestDTO.getZipCode())
                .country(requestDTO.getCountry())
                .emailVerified(true)
                .build();

        user = userRepository.save(user);
        otpStorage.remove(requestDTO.getEmail()); // cleanup

        return UserRegistrationResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .message("User registered successfully")
                .build();
    }

    @Override
    public User updateProfile(UUID userId, UpdateProfileRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update all updatable fields except username and password
        if (requestDTO.getName() != null) user.setName(requestDTO.getName());
        if (requestDTO.getEmail() != null) user.setEmail(requestDTO.getEmail());
        if (requestDTO.getPhone() != null) user.setPhone(requestDTO.getPhone());
        if (requestDTO.getAge() != null) user.setAge(requestDTO.getAge());
        if (requestDTO.getGender() != null) user.setGender(requestDTO.getGender());
        if (requestDTO.getStreet() != null) user.setStreet(requestDTO.getStreet());
        if (requestDTO.getCity() != null) user.setCity(requestDTO.getCity());
        if (requestDTO.getState() != null) user.setState(requestDTO.getState());
        if (requestDTO.getZipCode() != null) user.setZipCode(requestDTO.getZipCode());
        if (requestDTO.getCountry() != null) user.setCountry(requestDTO.getCountry());

        return userRepository.save(user);
    }

    @Override
    public UserProfileResponseDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .age(user.getAge())
                .gender(user.getGender())
                .street(user.getStreet())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .country(user.getCountry())
                .build();
    }

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
