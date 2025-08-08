package com.gdc.requests_management.exception;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException(String message) { super(message); }
}
