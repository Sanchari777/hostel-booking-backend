package com.hostelbooking.hostel_booking_backend.controller;

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
    public ResponseEntity<String> sendOtpForRegistration(@RequestBody HostelRegistrationRequest request) throws ExecutionException, InterruptedException {
        Hostel hostel = request.getHostel();
        if (hostel.getPhone() == null || hostel.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required.");
        }
        if (hostel.getName() == null || hostel.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Hostel name is required.");
        }

        QuerySnapshot existingPhoneHostel = db.collection("hostels")
                .whereEqualTo("phone", hostel.getPhone())
                .get()
                .get();
        if (!existingPhoneHostel.isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number is already EXIST.");
        }

        String deliveryMethod = request.getDeliveryMethod() != null ? request.getDeliveryMethod() : "mobile";
        String otp = otpService.generateOtp(null, hostel.getPhone());
        if (otpService.sendOtp(null, hostel.getPhone(), hostel.getEmail(), otp, deliveryMethod)) {
            return ResponseEntity.ok("OTP sent to " + (deliveryMethod.equalsIgnoreCase("email") ? hostel.getEmail() : hostel.getPhone()) + ". OTP (for testing): " + otp);
        } else {
            return ResponseEntity.status(500).body("Failed to send OTP.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHostel(@RequestBody HostelRegistrationRequest request) throws ExecutionException, InterruptedException {
        Hostel hostel = request.getHostel();
        if (hostel.getName() == null || hostel.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Hostel name is required.");
        }
        if (hostel.getPhone() == null || hostel.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required.");
        }

        if (!otpService.verifyOtp(null, hostel.getPhone(), request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        if (hostel.getEmail() != null && !hostel.getEmail().isEmpty()) {
            QuerySnapshot existingEmailHostel = db.collection("hostels")
                    .whereEqualTo("email", hostel.getEmail())
                    .get()
                    .get();
            if (!existingEmailHostel.isEmpty()) {
                return ResponseEntity.badRequest().body("Mail ID is already EXIST.");
            }
        }

        Hostel savedHostel = hostelService.addHostel(hostel);
        return ResponseEntity.ok(savedHostel);
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<String> sendLoginOtp(@RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getId() == null || loginRequest.getId().isEmpty()) {
            return ResponseEntity.badRequest().body("Hostel ID is required.");
        }
        if (loginRequest.getPhone() == null || loginRequest.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required.");
        }

        Hostel hostel = hostelService.getHostelById(loginRequest.getId());
        if (hostel == null) {
            return ResponseEntity.badRequest().body("Hostel ID not found.");
        }
        if (!hostel.getPhone().equals(loginRequest.getPhone())) {
            return ResponseEntity.badRequest().body("Phone number does not match the hostel ID.");
        }

        String otp = otpService.generateOtp(loginRequest.getId(), loginRequest.getPhone());
        String deliveryMethod = loginRequest.getDeliveryMethod() != null ? loginRequest.getDeliveryMethod() : "mobile";
        if (otpService.sendOtp(loginRequest.getId(), loginRequest.getPhone(), hostel.getEmail(), otp, deliveryMethod)) {
            return ResponseEntity.ok("Login OTP sent to " + (deliveryMethod.equalsIgnoreCase("email") ? hostel.getEmail() : loginRequest.getPhone()) + ". OTP (for testing): " + otp);
        } else {
            return ResponseEntity.status(500).body("Failed to send login OTP.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getId() == null || loginRequest.getId().isEmpty()) {
            return ResponseEntity.badRequest().body("Hostel ID is required.");
        }
        if (loginRequest.getPhone() == null || loginRequest.getPhone().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required.");
        }
        if (loginRequest.getOtp() == null || loginRequest.getOtp().isEmpty()) {
            return ResponseEntity.badRequest().body("OTP is required.");
        }

        Hostel hostel = hostelService.getHostelById(loginRequest.getId());
        if (hostel == null) {
            return ResponseEntity.badRequest().body("Hostel ID not found.");
        }
        if (!hostel.getPhone().equals(loginRequest.getPhone())) {
            return ResponseEntity.badRequest().body("Phone number does not match the hostel ID.");
        }

        if (!otpService.verifyOtp(loginRequest.getId(), loginRequest.getPhone(), loginRequest.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        return ResponseEntity.ok("Login successful for Hostel ID " + loginRequest.getId());
    }

    @GetMapping
    public ResponseEntity<List<Hostel>> getAllHostels() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(hostelService.getAllHostels());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hostel> updateHostel(@PathVariable String id, @RequestBody Hostel updatedHostel) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.updateHostel(id, updatedHostel);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hostel> getHostelById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.getHostelById(id);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.notFound().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Hostel>> getPendingHostels() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("hostels").whereEqualTo("approved", false).get().get();
        return ResponseEntity.ok(query.toObjects(Hostel.class));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Hostel> approveHostel(@PathVariable String id) throws ExecutionException, InterruptedException {
        Hostel hostel = hostelService.approveHostel(id);
        return hostel != null ? ResponseEntity.ok(hostel) : ResponseEntity.notFound().build();
    }
}

class HostelRegistrationRequest {
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

class LoginRequest {
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