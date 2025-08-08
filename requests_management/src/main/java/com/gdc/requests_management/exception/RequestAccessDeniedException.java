package com.gdc.requests_management.exception;

public class RequestAccessDeniedException extends RuntimeException {
    public RequestAccessDeniedException(String message) { super(message); }
}
