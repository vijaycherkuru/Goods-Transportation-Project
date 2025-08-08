package com.gdc.requests_management.exception;

public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(String message) { super(message); }
}
