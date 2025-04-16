package com.hostelbooking.hostel_booking_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hostel {
    private String id;
    @NotBlank(message = "Hostel name is required")
    private String name;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;
    @Email(message = "Invalid email format")
    private String email;
    private String ownerName;
    private String address;
    @Pattern(regexp = "\\d{6}", message = "Pin code must be 6 digits")
    private String pinCode;
    private String specification; // Mens, Womens, Co-living
    private List<String> stayingOptions; // Daily, Weekly, Monthly
    @Positive(message = "Price per day must be positive")
    private Double pricePerDay;
    private Double pricePerWeek;
    private Double pricePerMonth;
    private List<String> facilities;
    @PositiveOrZero(message = "Available rooms cannot be negative")
    private Integer availableRooms;
    @PositiveOrZero(message = "Available beds cannot be negative")
    private Integer availableBeds;
    private List<String> imageUrls;
    private String rulesAndPolicies;
    private boolean approved;
}