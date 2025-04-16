package com.hostelbooking.hostel_booking_backend.controller;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.dto.LoginRequest;
import com.hostelbooking.hostel_booking_backend.model.User;
import com.hostelbooking.hostel_booking_backend.service.OtpService;
import com.hostelbooking.hostel_booking_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final Firestore db = FirestoreClient.getFirestore();
    private final UserService userService;
    private final OtpService otpService;

    @Autowired
    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) throws ExecutionException, InterruptedException {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<?> sendUserLoginOtp(@Valid @RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
        }
        String otp = userService.generateUserOtp(loginRequest.getEmail());
        String deliveryMethod = loginRequest.getDeliveryMethod() != null ? loginRequest.getDeliveryMethod() : "email";
        if (otpService.sendOtp(null, null, loginRequest.getEmail(), otp, deliveryMethod)) {
            return ResponseEntity.ok(new SuccessResponse("OTP sent to " + loginRequest.getEmail()));
        } else {
            return ResponseEntity.status(500).body(new ErrorResponse("Failed to send OTP"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) throws ExecutionException, InterruptedException {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
        }
        if (loginRequest.getOtp() == null || loginRequest.getOtp().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("OTP is required"));
        }
        try {
            User user = userService.loginUser(loginRequest.getEmail(), loginRequest.getOtp());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) throws ExecutionException, InterruptedException {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(404).body(new ErrorResponse("User not found"));
    }
}