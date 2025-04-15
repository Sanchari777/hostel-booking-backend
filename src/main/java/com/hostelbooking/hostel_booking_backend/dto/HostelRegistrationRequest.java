package com.hostelbooking.hostel_booking_backend.dto;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import com.hostelbooking.hostel_booking_backend.service.HostelService;
import com.hostelbooking.hostel_booking_backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/hostels")

public class HostelRegistrationRequest {
    private Hostel hostel;
    private String otp;
    private String deliveryMethod;

    public Hostel getHostel() { return hostel; }
    public void setHostel(Hostel hostel) { this.hostel = hostel; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
}