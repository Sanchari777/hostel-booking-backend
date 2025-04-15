package com.hostelbooking.hostel_booking_backend.dto;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hostels")

public class LoginRequest {
    private String id;
    private String phone;
    private String otp;
    private String deliveryMethod;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

}