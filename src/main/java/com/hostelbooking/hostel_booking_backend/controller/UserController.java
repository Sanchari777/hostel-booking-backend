package com.hostelbooking.hostel_booking_backend.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Firestore db = FirestoreClient.getFirestore();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) throws ExecutionException, InterruptedException {
        // Check if email already exists
        QuerySnapshot existingEmailUsers = db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .get();

        if (!existingEmailUsers.isEmpty()) {
            return ResponseEntity.badRequest().body("Mail ID is already EXIST.");
        }

        // Check if phone number already exists
        QuerySnapshot existingPhoneUsers = db.collection("users")
                .whereEqualTo("phone", user.getPhone())
                .get()
                .get();

        if (!existingPhoneUsers.isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number is already EXIST.");
        }

        // Generate custom unique ID
        String customUserId = "USR-" + UUID.randomUUID().toString().substring(0, 8);
        user.setId(customUserId);

        // Save user to Firestore with custom ID
        db.collection("users").document(customUserId).set(user).get();

        return ResponseEntity.ok(user);
    }

    @GetMapping
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").get().get();
        return query.toObjects(User.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) throws ExecutionException, InterruptedException {
        var doc = db.collection("users").document(id).get().get();
        return doc.exists() ? ResponseEntity.ok(doc.toObject(User.class)) : ResponseEntity.notFound().build();
    }
}
