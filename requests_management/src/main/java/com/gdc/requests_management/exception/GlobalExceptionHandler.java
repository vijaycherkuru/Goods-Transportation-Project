package com.gdc.requests_management.exception;

import com.gdc.requests_management.dto.response.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleException(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body(
                StandardResponse.error(500, ex.getMessage(), req.getRequestURI()));
    }
}