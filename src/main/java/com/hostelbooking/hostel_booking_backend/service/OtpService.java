package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Service
public class OtpService {
    private final Firestore db = FirestoreClient.getFirestore();

    public String generateOtp(String id, String phone) throws ExecutionException, InterruptedException {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required for OTP generation.");
        }
        String key = (id != null ? id : "REG") + ":" + phone;
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expirationTime = System.currentTimeMillis() + 300_000; // 5 minutes
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("expirationTime", expirationTime);
        otpData.put("phone", phone);
        db.collection("otps").document(key).set(otpData).get();
        System.out.println("Generated OTP for " + key + ": " + otp);
        return otp;
    }

    public boolean sendOtp(String id, String phone, String email, String otp, String deliveryMethod) {
        String key = (id != null ? id : "REG") + ":" + phone;
        String effectiveDeliveryMethod = (deliveryMethod != null && "email".equalsIgnoreCase(deliveryMethod)) ? "email" : "mobile";

        try {
            if ("mobile".equals(effectiveDeliveryMethod)) {
                System.out.println("Simulated SMS OTP for " + phone + ": " + otp);
                return true;
            } else if ("email".equals(effectiveDeliveryMethod)) {
                if (email == null || email.isEmpty()) {
                    System.err.println("Email is required for email delivery.");
                    return false;
                }
                System.out.println("Simulated email OTP to " + email + ": " + otp);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to send OTP: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyOtp(String id, String phone, String otp) throws ExecutionException, InterruptedException {
        if (phone == null || phone.isEmpty()) {
            System.out.println("Phone number is required for OTP verification.");
            return false;
        }
        String key = (id != null ? id : "REG") + ":" + phone;
        var doc = db.collection("otps").document(key).get().get();
        if (!doc.exists()) {
            System.out.println("No OTP found for " + key);
            return false;
        }
        Map<String, Object> otpData = doc.getData();
        String storedOtp = (String) otpData.get("otp");
        long expirationTime = (long) otpData.get("expirationTime");
        if (System.currentTimeMillis() > expirationTime) {
            db.collection("otps").document(key).delete().get();
            System.out.println("OTP expired for " + key);
            return false;
        }
        if (!storedOtp.equals(otp)) {
            System.out.println("Invalid OTP for " + key + ": expected " + storedOtp + ", got " + otp);
            return false;
        }
        db.collection("otps").document(key).delete().get();
        System.out.println("OTP verified successfully for " + key);
        return true;
    }
}