package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore db = FirestoreClient.getFirestore();

    public User registerUser(User user) throws ExecutionException, InterruptedException {
        // Check if the email already exists
        QuerySnapshot existingEmail = db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .get();

        if (!existingEmail.isEmpty()) {
            throw new RuntimeException("Mail ID is already EXIST.");
        }

        // Check if the phone number already exists
        QuerySnapshot existingPhone = db.collection("users")
                .whereEqualTo("phone", user.getPhone())
                .get()
                .get();

        if (!existingPhone.isEmpty()) {
            throw new RuntimeException("Mobile number is already EXIST.");
        }

        // Generate a unique ID and save the user
        DocumentReference docRef = db.collection("users").document();
        user.setId(docRef.getId());
        docRef.set(user).get();
        return user;
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("users").get().get();
        return query.toObjects(User.class);
    }

    public User getUserById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(id).get().get();
        return doc.exists() ? doc.toObject(User.class) : null;
    }
}
