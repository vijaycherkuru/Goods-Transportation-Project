package com.gdc.requests_management.exception;

public class RequestAlreadyAssignedException extends RuntimeException {
    public RequestAlreadyAssignedException(String message) { super(message); }
}