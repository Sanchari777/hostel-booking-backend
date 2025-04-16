package com.hostelbooking.hostel_booking_backend.dto;

import com.hostelbooking.hostel_booking_backend.model.Hostel;
import lombok.Data;

@Data
public class HostelRegistrationRequest {
    private Hostel hostel;
    private String otp;
    private String deliveryMethod;
}