package com.gdc.requests_management.exception;

public class InvalidRequestStatusException extends RuntimeException {
    public InvalidRequestStatusException(String message) {
        super(message);
    }
}