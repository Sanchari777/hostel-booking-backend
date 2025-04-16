package com.hostelbooking.hostel_booking_backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String id;
    private String phone;
    private String email;
    private String otp;
    private String deliveryMethod;
}