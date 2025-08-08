package com.gdc.requests_management.exception;

public class RideCapacityExceededException extends RuntimeException {
    public RideCapacityExceededException(String message) { super(message); }
}