package com.hostelbooking.hostel_booking_backend.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.hostelbooking.hostel_booking_backend.model.Booking;
import com.hostelbooking.hostel_booking_backend.service.HostelBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final Firestore db;
    private final HostelBookingService bookingService;

    @Autowired
    public BookingController(Firestore db, HostelBookingService bookingService) {
        this.db = db;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        try {
            Booking createdBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(createdBooking);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(@PathVariable String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("bookings").whereEqualTo("userId", userId).get().get();
        return query.toObjects(Booking.class);
    }

    @GetMapping("/hostel/{hostelId}")
    public List<Booking> getHostelBookings(@PathVariable String hostelId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("bookings").whereEqualTo("hostelId", hostelId).get().get();
        return query.toObjects(Booking.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable String id, @RequestBody Booking updatedBooking) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("bookings").document(id);
        DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            return ResponseEntity.notFound().build();
        }
        docRef.set(updatedBooking).get();
        return ResponseEntity.ok(updatedBooking);
    }
}