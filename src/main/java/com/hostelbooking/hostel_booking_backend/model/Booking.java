package com.hostelbooking.hostel_booking_backend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private String id;
    @NotBlank(message = "Hostel ID is required")
    private String hostelId;
    @NotBlank(message = "User ID is required")
    private String userId;
    @Min(value = 1, message = "At least one bed must be booked")
    private int numberOfBeds;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    @Positive(message = "Total price must be positive")
    private double totalPrice;
}