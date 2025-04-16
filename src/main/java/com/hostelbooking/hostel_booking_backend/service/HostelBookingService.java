package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.Booking;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@Service
public class HostelBookingService {
    private final Firestore db = FirestoreClient.getFirestore();

    public Booking createBooking(@Valid Booking booking) throws ExecutionException, InterruptedException {
        DocumentReference hostelRef = db.collection("hostels").document(booking.getHostelId());
        DocumentSnapshot document = hostelRef.get().get();
        if (!document.exists()) {
            throw new RuntimeException("Hostel not found");
        }
        Hostel hostel = document.toObject(Hostel.class);
        if (hostel.getAvailableBeds() < booking.getNumberOfBeds()) {
            throw new RuntimeException("Not enough beds available");
        }

        long days = booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay();
        if (days <= 0) {
            throw new RuntimeException("End date must be after start date");
        }
        booking.setTotalPrice(booking.getNumberOfBeds() * hostel.getPricePerDay() * days);

        hostel.setAvailableBeds(hostel.getAvailableBeds() - booking.getNumberOfBeds());
        hostelRef.set(hostel).get();

        DocumentReference bookingRef = db.collection("bookings").document();
        booking.setId(bookingRef.getId());
        bookingRef.set(booking).get();
        return booking;
    }

    public void cancelBooking(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);
        DocumentSnapshot bookingDoc = bookingRef.get().get();
        if (!bookingDoc.exists()) {
            throw new RuntimeException("Booking not found");
        }
        Booking booking = bookingDoc.toObject(Booking.class);

        DocumentReference hostelRef = db.collection("hostels").document(booking.getHostelId());
        DocumentSnapshot hostelDoc = hostelRef.get().get();
        if (!hostelDoc.exists()) {
            throw new RuntimeException("Hostel not found");
        }
        Hostel hostel = hostelDoc.toObject(Hostel.class);
        hostel.setAvailableBeds(hostel.getAvailableBeds() + booking.getNumberOfBeds());
        hostelRef.set(hostel).get();

        bookingRef.delete().get();
    }
}