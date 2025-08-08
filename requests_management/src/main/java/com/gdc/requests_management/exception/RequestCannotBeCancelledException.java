package com.gdc.requests_management.exception;

public class RequestCannotBeCancelledException extends RuntimeException {
    public RequestCannotBeCancelledException(String message) { super(message); }
}