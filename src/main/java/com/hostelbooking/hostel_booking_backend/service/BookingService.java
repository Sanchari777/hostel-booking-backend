package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.hostelbooking.hostel_booking_backend.model.Booking;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private HostelService hostelService;

    public Booking createBooking(@Valid Booking booking) {
        try {
            if (!hostelExists(booking.getHostelId())) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            if (!userExists(booking.getUserId())) {
                throw new IllegalArgumentException("User ID does not exist");
            }
            Hostel hostel = hostelService.getHostelById(booking.getHostelId());
            if (hostel.getAvailableBeds() < booking.getNumberOfBeds()) {
                throw new IllegalArgumentException("Not enough beds available");
            }
            String id = "BOOK-" + System.currentTimeMillis();
            booking.setId(id);
            double pricePerDay = hostel.getPricePerDay();
            long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
            if (days <= 0) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            double totalPrice = pricePerDay * days * booking.getNumberOfBeds();
            booking.setTotalPrice(totalPrice);
            hostel.setAvailableBeds(hostel.getAvailableBeds() - booking.getNumberOfBeds());
            firestore.collection("hostels").document(hostel.getId()).set(hostel).get();
            firestore.collection("bookings").document(id).set(booking).get();
            return booking;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
    }

    public List<Booking> getAllBookings() {
        try {
            List<Booking> bookings = new ArrayList<>();
            for (QueryDocumentSnapshot doc : firestore.collection("bookings").get().get().getDocuments()) {
                bookings.add(doc.toObject(Booking.class));
            }
            return bookings;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch bookings: " + e.getMessage());
        }
    }

    public List<Booking> getUserBookings(String userId) {
        try {
            if (!userExists(userId)) {
                throw new IllegalArgumentException("User ID does not exist");
            }
            List<Booking> bookings = new ArrayList<>();
            for (QueryDocumentSnapshot doc : firestore.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .get()
                    .getDocuments()) {
                bookings.add(doc.toObject(Booking.class));
            }
            return bookings;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch user bookings: " + e.getMessage());
        }
    }

    public List<Booking> getHostelBookings(String hostelId) {
        try {
            if (!hostelExists(hostelId)) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            List<Booking> bookings = new ArrayList<>();
            for (QueryDocumentSnapshot doc : firestore.collection("bookings")
                    .whereEqualTo("hostelId", hostelId)
                    .get()
                    .get()
                    .getDocuments()) {
                bookings.add(doc.toObject(Booking.class));
            }
            return bookings;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch hostel bookings: " + e.getMessage());
        }
    }

    public Booking updateBooking(String id, @Valid Booking booking) {
        try {
            DocumentSnapshot snapshot = firestore.collection("bookings").document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Booking ID does not exist");
            }
            Booking existingBooking = snapshot.toObject(Booking.class);
            Hostel hostel = hostelService.getHostelById(booking.getHostelId());
            int bedDifference = booking.getNumberOfBeds() - existingBooking.getNumberOfBeds();
            if (bedDifference > 0 && hostel.getAvailableBeds() < bedDifference) {
                throw new IllegalArgumentException("Not enough beds available");
            }
            if (!hostelExists(booking.getHostelId())) {
                throw new IllegalArgumentException("Hostel ID does not exist");
            }
            if (!userExists(booking.getUserId())) {
                throw new IllegalArgumentException("User ID does not exist");
            }
            double pricePerDay = hostel.getPricePerDay();
            long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
            if (days <= 0) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            double totalPrice = pricePerDay * days * booking.getNumberOfBeds();
            booking.setTotalPrice(totalPrice);
            booking.setId(id);
            hostel.setAvailableBeds(hostel.getAvailableBeds() - bedDifference);
            firestore.collection("hostels").document(hostel.getId()).set(hostel).get();
            firestore.collection("bookings").document(id).set(booking).get();
            return booking;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update booking: " + e.getMessage());
        }
    }

    public void cancelBooking(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection("bookings").document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Booking ID does not exist");
            }
            Booking booking = snapshot.toObject(Booking.class);
            Hostel hostel = hostelService.getHostelById(booking.getHostelId());
            hostel.setAvailableBeds(hostel.getAvailableBeds() + booking.getNumberOfBeds());
            firestore.collection("hostels").document(hostel.getId()).set(hostel).get();
            firestore.collection("bookings").document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }

    private boolean hostelExists(String hostelId) throws InterruptedException, ExecutionException {
        return firestore.collection("hostels").document(hostelId).get().get().exists();
    }

    private boolean userExists(String userId) throws InterruptedException, ExecutionException {
        return firestore.collection("users").document(userId).get().get().exists();
    }

    private double getHostelPricePerDay(String hostelId) {
        Hostel hostel = hostelService.getHostelById(hostelId);
        return hostel.getPricePerDay() != null ? hostel.getPricePerDay() : 0.0;
    }
}