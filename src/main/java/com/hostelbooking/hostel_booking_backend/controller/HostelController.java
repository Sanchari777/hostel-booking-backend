package com.hostelbooking.hostel_booking_backend.controller;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.dto.HostelRegistrationRequest;
import com.hostelbooking.hostel_booking_backend.dto.LoginRequest;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import com.hostelbooking.hostel_booking_backend.service.HostelService;
import com.hostelbooking.hostel_booking_backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/hostels")
@Validated
public class HostelController {

    private final Firestore db = FirestoreClient.getFirestore();
    private final HostelService hostelService;
    private final OtpService otpService;

    @Autowired
    public HostelController(HostelService hostelService, OtpService otpService) {
        this.hostelService = hostelService;
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtpForRegistration(@Valid @RequestBody HostelRegistrationRequest request) throws ExecutionException, InterruptedException {
        Hostel hostel = request.getHostel();
        if (hostel.getPhone() == null || hostel.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number is required"));
        }
        if (hostel.getName() == null || hostel.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel name is required"));
        }

        QuerySnapshot existingPhoneHostel = db.collection("hostels")
                .whereEqualTo("phone", hostel.getPhone())
                .get()
                .get();
        if (!existingPhoneHostel.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number already exists"));
        }

        String deliveryMethod = request.getDeliveryMethod() != null ? request.getDeliveryMethod() : "mobile";
        String otp = otpService.generateOtp(null, hostel.getPhone());
        if (otpService.sendOtp(null, hostel.getPhone(), hostel.getEmail(), otp, deliveryMethod)) {
            return ResponseEntity.ok(new SuccessResponse("OTP sent to " + (deliveryMethod.equalsIgnoreCase("email") ? hostel.getEmail() : hostel.getPhone())));
        } else {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to send OTP"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHostel(@Valid @RequestBody HostelRegistrationRequest request) throws ExecutionException, InterruptedException {
        Hostel hostel = request.getHostel();
        if (hostel.getName() == null || hostel.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel name is required"));
        }
        if (hostel.getPhone() == null || hostel.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number is required"));
        }

        if (!otpService.verifyOtp(null, hostel.getPhone(), request.getOtp())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid or expired OTP"));
        }

        if (hostel.getEmail() != null && !hostel.getEmail().isEmpty()) {
            QuerySnapshot existingEmailHostel = db.collection("hostels")
                    .whereEqualTo("email", hostel.getEmail())
                    .get()
                    .get();
            if (!existingEmailHostel.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
            }
        }

        Hostel savedHostel = hostelService.addHostel(hostel);
        return ResponseEntity.ok(savedHostel);
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<?> sendLoginOtp(@Valid @RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getId() == null || loginRequest.getId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel ID is required"));
        }
        if (loginRequest.getPhone() == null || loginRequest.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number is required"));
        }

        Hostel hostel = hostelService.getHostelById(loginRequest.getId());
        if (hostel == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel ID not found"));
        }
        if (!hostel.getPhone().equals(loginRequest.getPhone())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number does not match hostel ID"));
        }

        String otp = otpService.generateOtp(loginRequest.getId(), loginRequest.getPhone());
        String deliveryMethod = loginRequest.getDeliveryMethod() != null ? loginRequest.getDeliveryMethod() : "mobile";
        if (otpService.sendOtp(loginRequest.getId(), loginRequest.getPhone(), hostel.getEmail(), otp, deliveryMethod)) {
            return ResponseEntity.ok(new SuccessResponse("OTP sent to " + (deliveryMethod.equalsIgnoreCase("email") ? hostel.getEmail() : loginRequest.getPhone())));
        } else {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to send OTP"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getId() == null || loginRequest.getId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel ID is required"));
        }
        if (loginRequest.getPhone() == null || loginRequest.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number is required"));
        }
        if (loginRequest.getOtp() == null || loginRequest.getOtp().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("OTP is required"));
        }

        Hostel hostel = hostelService.getHostelById(loginRequest.getId());
        if (hostel == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Hostel ID not found"));
        }
        if (!hostel.getPhone().equals(loginRequest.getPhone())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Phone number does not match hostel ID"));
        }

        if (!otpService.verifyOtp(loginRequest.getId(), loginRequest.getPhone(), loginRequest.getOtp())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid or expired OTP"));
        }

        return ResponseEntity.ok(new SuccessResponse("Login successful for Hostel ID " + loginRequest.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Hostel>> getAllHostels() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(hostelService.getAllHostels());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHostel(@PathVariable String id, @Valid @RequestBody Hostel updatedHostel) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.updateHostel(id, updatedHostel);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.status(404).body(new ErrorResponse("Hostel not found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHostelById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.getHostelById(id);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.status(404).body(new ErrorResponse("Hostel not found"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Hostel>> getPendingHostels() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("hostels").whereEqualTo("approved", false).get().get();
        return ResponseEntity.ok(query.toObjects(Hostel.class));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveHostel(@PathVariable String id) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.approveHostel(id);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.status(404).body(new ErrorResponse("Hostel not found"));
    }
}