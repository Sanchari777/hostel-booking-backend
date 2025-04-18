package com.hostelbooking.hostel_booking_backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String otp;
}