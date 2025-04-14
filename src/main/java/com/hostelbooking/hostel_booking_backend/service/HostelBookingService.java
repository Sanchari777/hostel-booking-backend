package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.hostelbooking.hostel_booking_backend.model.Booking;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class HostelBookingService {

    private final Firestore db;

    @Autowired
    public HostelBookingService(Firestore db) {
        this.db = db;
    }

    public Booking createBooking(Booking booking) throws ExecutionException, InterruptedException {
        // Fetch hostel using hostelId from booking
        DocumentReference hostelRef = db.collection("hostels").document(booking.getHostelId());
        DocumentSnapshot document = hostelRef.get().get();

        if (!document.exists()) {
            throw new RuntimeException("Hostel not found");
        }

        Hostel hostel = document.toObject(Hostel.class);
        if (hostel.getAvailableBeds() < booking.getNumberOfBeds()) {
            throw new RuntimeException("Not enough beds available");
        }

        // Update hostel availability
        hostel.setAvailableBeds(hostel.getAvailableBeds() - booking.getNumberOfBeds());
        hostelRef.set(hostel).get(); // Ensure the update is completed

        // Calculate total price
        long days = booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay();
        if (days <= 0) {
            throw new RuntimeException("End date must be after start date");
        }
        booking.setTotalPrice(booking.getNumberOfBeds() * hostel.getPricePerDay() * days);

        // Save booking
        DocumentReference bookingRef = db.collection("bookings").document();
        booking.setId(bookingRef.getId());
        bookingRef.set(booking).get(); // Ensure the save is completed

        return booking;
    }
}