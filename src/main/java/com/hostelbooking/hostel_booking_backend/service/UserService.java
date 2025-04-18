package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private OtpService otpService;

    public User registerUser(User user) throws ExecutionException, InterruptedException {
        QuerySnapshot existingPhone = db.collection("users")
                .whereEqualTo("phone", user.getPhone())
                .get()
                .get();
        if (!existingPhone.isEmpty()) {
            throw new RuntimeException("Phone number already exists");
        }
        String customUserId = "USR-" + UUID.randomUUID().toString().substring(0, 8);
        user.setId(customUserId);
        db.collection("users").document(customUserId).set(user).get();
        return user;
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").get().get();
        return query.toObjects(User.class);
    }

    public User getUserById(String id) throws ExecutionException, InterruptedException {
        var doc = db.collection("users").document(id).get().get();
        return doc.exists() ? doc.toObject(User.class) : null;
    }

    public String sendUserOtp(String phone) throws ExecutionException, InterruptedException {
        var query = db.collection("users").whereEqualTo("phone", phone).get().get();
        if (query.isEmpty()) {
            User user = new User();
            user.setPhone(phone);
            user.setRole("USER");
            user.setName("Guest");
            return registerUser(user).getId();
        }
        User user = query.toObjects(User.class).get(0);
        String otp = otpService.generateOtp(user.getId(), phone);
        // --- Added Logging ---
        System.out.println("Generating OTP for userId: " + user.getId() + ", phone: " + phone + ", otp: " + otp);
        try {
            otpService.sendOtp(user.getId(), phone, null, otp, "mobile");
            System.out.println("OTP sent successfully for phone: " + phone);
        } catch (Exception e) {
            System.err.println("Failed to send OTP for phone: " + phone + ", error: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP: " + e.getMessage());
        }
        // --- End Added Logging ---
        return "OTP sent to " + phone;
    }

    public User loginUser(String phone, String otp) throws ExecutionException, InterruptedException {
        var query = db.collection("users").whereEqualTo("phone", phone).get().get();
        if (query.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = query.toObjects(User.class).get(0);
        // --- Added Logging ---
        System.out.println("Verifying OTP for userId: " + user.getId() + ", phone: " + phone + ", otp: " + otp);
        boolean isValidOtp = otpService.verifyOtp(user.getId(), phone, otp);
        System.out.println("OTP verification result for phone: " + phone + ", valid: " + isValidOtp);
        // --- End Added Logging ---
        if (!isValidOtp) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        return user;
    }
}