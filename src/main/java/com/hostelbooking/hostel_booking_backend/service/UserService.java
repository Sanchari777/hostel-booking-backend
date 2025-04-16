package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db = FirestoreClient.getFirestore();

    public User registerUser(User user) throws ExecutionException, InterruptedException {
        QuerySnapshot existingEmail = db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .get();
        if (!existingEmail.isEmpty()) {
            throw new RuntimeException("Email already exists");
        }
        QuerySnapshot existingPhone = db.collection("users")
                .whereEqualTo("phone", user.getPhone())
                .get()
                .get();
        if (!existingPhone.isEmpty()) {
            throw new RuntimeException("Phone number already exists");
        }

        String customUserId = "USR-" + UUID.randomUUID().toString().substring(0, 8);
        user.setId(customUserId);
        DocumentReference docRef = db.collection("users").document(customUserId);
        docRef.set(user).get();
        return user;
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").get().get();
        return query.toObjects(User.class);
    }

    public User getUserById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(id).get().get();
        return doc.exists() ? doc.toObject(User.class) : null;
    }

    public User loginUser(String email, String otp) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").whereEqualTo("email", email).get().get();
        if (query.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = query.toObjects(User.class).get(0);
        String key = "USER:" + email;
        var doc = db.collection("otps").document(key).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("No OTP found");
        }
        Map<String, Object> otpData = doc.getData();
        String storedOtp = (String) otpData.get("otp");
        long expirationTime = (long) otpData.get("expirationTime");
        if (System.currentTimeMillis() > expirationTime) {
            db.collection("otps").document(key).delete().get();
            throw new RuntimeException("OTP expired");
        }
        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        db.collection("otps").document(key).delete().get();
        return user;
    }

    public String generateUserOtp(String email) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").whereEqualTo("email", email).get().get();
        if (query.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        String key = "USER:" + email;
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expirationTime = System.currentTimeMillis() + 300_000;
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("expirationTime", expirationTime);
        otpData.put("email", email);
        db.collection("otps").document(key).set(otpData).get();
        System.out.println("Generated user OTP for " + email + ": " + otp);
        return otp;
    }
}