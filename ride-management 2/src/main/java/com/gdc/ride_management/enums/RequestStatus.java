package com.gdc.ride_management.enums;

public enum RequestStatus {
    PENDING,     // Request is sent but not yet processed
    ACCEPTED,    // Request has been approved by the driver
    REJECTED,    // Request was declined
    CANCELLED,   // Request was canceled by the sender
    COMPLETED    // Ride request was successfully completed

}
