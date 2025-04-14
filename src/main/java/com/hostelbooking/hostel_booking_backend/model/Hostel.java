package com.hostelbooking.hostel_booking_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hostel {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String ownerName;
    private String address;
    private String pinCode;
    private String specification; // "Mens", "Womens", "Co-living"
    private List<String> stayingOptions; // e.g., ["Daily", "Weekly", "Monthly"]
    private Double pricePerDay;
    private Double pricePerWeek;
    private Double pricePerMonth;
    private List<String> facilities; // e.g., ["WiFi", "AC", "Laundry"]
    private Integer availableRooms;
    private Integer availableBeds;
    private List<String> imageUrls; // URLs after upload
    private String rulesAndPolicies;
    private boolean approved;
}