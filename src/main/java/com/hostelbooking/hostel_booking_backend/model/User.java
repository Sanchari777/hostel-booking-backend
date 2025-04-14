package com.hostelbooking.hostel_booking_backend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id; // Firestore uses String IDs
    private String email;
    private String password; // Plaintext for now; hash later
    private String role; // "USER", "OWNER", "ADMIN"
    private String name;
    private String phone;
}