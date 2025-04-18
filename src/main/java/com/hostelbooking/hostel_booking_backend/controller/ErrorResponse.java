package com.hostelbooking.hostel_booking_backend.controller;

public class ErrorResponse {
    private String error;
    public ErrorResponse(String error) { this.error = error; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}