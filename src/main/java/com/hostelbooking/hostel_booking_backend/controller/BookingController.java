package com.hostelbooking.hostel_booking_backend.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.hostelbooking.hostel_booking_backend.model.Booking;
import com.hostelbooking.hostel_booking_backend.service.HostelBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final Firestore db;
    private final HostelBookingService bookingService;

    @Autowired
    public BookingController(Firestore db, HostelBookingService bookingService) {
        this.db = db;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody Booking booking) {
        try {
            Booking createdBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(createdBooking);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("bookings").get().get();
        return ResponseEntity.ok(query.toObjects(Booking.class));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("bookings").whereEqualTo("userId", userId).get().get();
        return ResponseEntity.ok(query.toObjects(Booking.class));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<?> getHostelBookings(@PathVariable String hostelId) throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("bookings").whereEqualTo("hostelId", hostelId).get().get();
        return ResponseEntity.ok(query.toObjects(Booking.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable String id, @Valid @RequestBody Booking updatedBooking) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("bookings").document(id);
        DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            return ResponseEntity.status(404).body(new ErrorResponse("Booking not found"));
        }
        docRef.set(updatedBooking).get();
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable String id) throws ExecutionException, InterruptedException {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok(new SuccessResponse("Booking cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }
}

class ErrorResponse {
    private String error;
    public ErrorResponse(String error) { this.error = error; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

class SuccessResponse {
    private String message;
    public SuccessResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}