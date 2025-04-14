package com.hostelbooking.hostel_booking_backend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private String id;           // Booking ID
    private String hostelId;     // Hostel ID
    private String userId;       // User ID (optional, based on your controller)
    private int numberOfBeds;    // Number of beds booked
    private LocalDate startDate; // Start date of booking
    private LocalDate endDate;   // End date of booking
    private double totalPrice;   // Total price of the booking
}