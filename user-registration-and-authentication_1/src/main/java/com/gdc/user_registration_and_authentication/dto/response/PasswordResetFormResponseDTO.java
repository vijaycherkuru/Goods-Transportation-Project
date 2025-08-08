package com.gdc.user_registration_and_authentication.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PasswordResetFormResponseDTO {
    private boolean success;
    private String message;
    private boolean showPasswordForm;
    private String email;
    private String otp;
    private List<String> passwordRequirements;
}
