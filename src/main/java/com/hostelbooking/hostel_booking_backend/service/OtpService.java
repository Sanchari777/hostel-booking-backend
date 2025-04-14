package com.hostelbooking.hostel_booking_backend.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStore = new HashMap<>();

    public String generateOtp(String id, String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required for OTP generation.");
        }
        String key = (id != null ? id : "REG") + ":" + phone; // Use "REG" for registration
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        otpStore.put(key, new OtpData(otp, expirationTime));
        System.out.println("Generated OTP for " + key + ": " + otp);
        return otp;
    }

    public boolean sendOtp(String id, String phone, String email, String otp, String deliveryMethod) {
        String key = (id != null ? id : "REG") + ":" + phone;

        // Default to "mobile" if deliveryMethod is null or invalid
        String effectiveDeliveryMethod = (deliveryMethod != null && "email".equalsIgnoreCase(deliveryMethod)) ? "email" : "mobile";

        try {
            if ("mobile".equals(effectiveDeliveryMethod)) {
                if (phone == null || phone.isEmpty()) {
                    System.err.println("Failed to send OTP: Phone number is required for mobile delivery.");
                    return false;
                }
                System.out.println("Simulated OTP SMS sent to " + phone + " for " + key + ": " + otp);
            } else if ("email".equals(effectiveDeliveryMethod)) {
                if (email == null || email.isEmpty()) {
                    System.err.println("Failed to send OTP: Email is required for email delivery.");
                    return false;
                }
                // Placeholder for email sending (e.g., JavaMailSender)
                System.out.println("Simulated OTP email sent to " + email + " for " + key + ": " + otp);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send OTP to " + (effectiveDeliveryMethod.equals("mobile") ? phone : email) + " for " + key + ": " + e.getMessage());
            return false;
        }
    }

    public boolean verifyOtp(String id, String phone, String otp) {
        if (phone == null || phone.isEmpty()) {
            System.out.println("Phone number is required for OTP verification.");
            return false;
        }
        String key = (id != null ? id : "REG") + ":" + phone;
        OtpData otpData = otpStore.get(key);
        if (otpData == null) {
            System.out.println("No OTP found for " + key);
            return false;
        }
        if (System.currentTimeMillis() > otpData.getExpirationTime()) {
            otpStore.remove(key);
            System.out.println("OTP expired for " + key);
            return false;
        }
        if (!otpData.getOtp().equals(otp)) {
            System.out.println("Invalid OTP for " + key + ": expected " + otpData.getOtp() + ", got " + otp);
            return false;
        }
        otpStore.remove(key);
        System.out.println("OTP verified successfully for " + key);
        return true;
    }

    private static class OtpData {
        private final String otp;
        private final long expirationTime;

        public OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}