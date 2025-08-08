package com.gdc.requests_management.exception;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(String message) { super(message); }
}