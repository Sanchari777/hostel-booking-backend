package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class HostelService {

    @Autowired
    private Firestore db;

    @Autowired
    private OtpService otpService;

    public String sendHostelOtp(String phone, String deliveryMethod) {
        try {
            if (phone == null || !phone.matches("\\d{10}")) {
                throw new IllegalArgumentException("Invalid phone number");
            }
            if (!"mobile".equals(deliveryMethod)) {
                throw new IllegalArgumentException("Only mobile delivery method is supported");
            }
            String otp = otpService.generateOtp("REG", phone);
            otpService.sendOtp("REG", phone, null, otp, deliveryMethod);
            return "OTP sent to " + phone;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to send OTP: " + e.getMessage());
        }
    }

    public Hostel registerHostel(Hostel hostel, String otp) {
        try {
            if (!otpService.verifyOtp("REG", hostel.getPhone(), otp)) {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
            String id = "HSTL-" + UUID.randomUUID().toString().substring(0, 8);
            hostel.setId(id);
            hostel.setApproved(false);
            db.collection("hostels").document(id).set(hostel).get();
            return hostel;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to register hostel: " + e.getMessage());
        }
    }

    public String sendHostelLoginOtp(String id, String phone, String deliveryMethod) {
        try {
            DocumentSnapshot hostelDoc = db.collection("hostels").document(id).get().get();
            if (!hostelDoc.exists() || !hostelDoc.toObject(Hostel.class).getPhone().equals(phone)) {
                throw new IllegalArgumentException("Invalid hostel ID or phone number");
            }
            if (!"mobile".equals(deliveryMethod)) {
                throw new IllegalArgumentException("Only mobile delivery method is supported");
            }
            String otp = otpService.generateOtp(id, phone);
            otpService.sendOtp(id, phone, null, otp, deliveryMethod);
            return "OTP sent to " + phone;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to send login OTP: " + e.getMessage());
        }
    }

    public Hostel loginHostel(String id, String phone, String otp) {
        try {
            if (!otpService.verifyOtp(id, phone, otp)) {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
            DocumentSnapshot hostelDoc = db.collection("hostels").document(id).get().get();
            if (!hostelDoc.exists() || !hostelDoc.toObject(Hostel.class).getPhone().equals(phone)) {
                throw new IllegalArgumentException("Invalid hostel ID or phone number");
            }
            return hostelDoc.toObject(Hostel.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to login: " + e.getMessage());
        }
    }

    public Hostel updateHostel(String id, Hostel hostel) {
        try {
            DocumentSnapshot snapshot = db.collection("hostels").document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            hostel.setId(id);
            hostel.setApproved(snapshot.toObject(Hostel.class).isApproved());
            db.collection("hostels").document(id).set(hostel).get();
            return hostel;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update hostel: " + e.getMessage());
        }
    }

    public List<Hostel> getPendingHostels() {
        try {
            List<Hostel> hostels = new ArrayList<>();
            for (QueryDocumentSnapshot doc : db.collection("hostels")
                    .whereEqualTo("approved", false)
                    .get()
                    .get()
                    .getDocuments()) {
                hostels.add(doc.toObject(Hostel.class));
            }
            return hostels;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch pending hostels: " + e.getMessage());
        }
    }

    public Hostel approveHostel(String id) {
        try {
            DocumentSnapshot snapshot = db.collection("hostels").document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            Hostel hostel = snapshot.toObject(Hostel.class);
            hostel.setApproved(true);
            db.collection("hostels").document(id).set(hostel).get();
            return hostel;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to approve hostel: " + e.getMessage());
        }
    }

    public Hostel getHostelById(String id) {
        try {
            DocumentSnapshot snapshot = db.collection("hostels").document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            return snapshot.toObject(Hostel.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch hostel: " + e.getMessage());
        }
    }

    public List<Hostel> getHostels() {
        try {
            List<Hostel> hostels = new ArrayList<>();
            for (QueryDocumentSnapshot doc : db.collection("hostels")
                    .whereEqualTo("approved", true)
                    .get()
                    .get()
                    .getDocuments()) {
                hostels.add(doc.toObject(Hostel.class));
            }
            return hostels;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch hostels: " + e.getMessage());
        }
    }
}