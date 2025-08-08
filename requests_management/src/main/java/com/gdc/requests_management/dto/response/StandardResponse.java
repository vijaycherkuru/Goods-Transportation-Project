package com.gdc.requests_management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    private int status;

    private Boolean success;


    private String message;

    private T data;

    private Object errors;

    private String path;

    // ✅ Success 200
    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> success(T data) {
        return success(data, "Request processed successfully");
    }

    // ✅ Created 201
    public static <T> StandardResponse<T> created(T data, String message) {
        return StandardResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> created(T data) {
        return created(data, "Resource created successfully");
    }

    // ✅ No Content 204
    public static <T> StandardResponse<T> noContent(String message) {
        return StandardResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(204)
                .message(message)
                .build();
    }

    // ✅ Generic Error
    public static <T> StandardResponse<T> error(int status, String message, String path) {
        return StandardResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .path(path)
                .build();
    }

    public static <T> StandardResponse<T> error(int status, String message, Object errors, String path) {
        return StandardResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .errors(errors)
                .path(path)
                .build();
    }

    // ✅ Common Error Responses
    public static <T> StandardResponse<T> badRequest(String message, String path) {
        return error(400, message, path);
    }

    public static <T> StandardResponse<T> unauthorized(String message, String path) {
        return error(401, message, path);
    }

    public static <T> StandardResponse<T> forbidden(String message, String path) {
        return error(403, message, path);
    }

    public static <T> StandardResponse<T> notFound(String message, String path) {
        return error(404, message, path);
    }

    public static <T> StandardResponse<T> internalServerError(String message, String path) {
        return error(500, message, path);
    }
}
