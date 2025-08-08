package com.gdc.user_registration_and_authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private T data;
    private String message;
    private boolean success;

    public static <T> StandardResponse<T> ok(T data) {
        return new StandardResponse<>(data, "Request successful", true);
    }

    public static <T> StandardResponse<T> of(T data, String message) {
        return new StandardResponse<>(data, message, true);
    }

    public static <T> StandardResponse<T> error(String message) {
        return new StandardResponse<>(null, message, false);
    }
}
