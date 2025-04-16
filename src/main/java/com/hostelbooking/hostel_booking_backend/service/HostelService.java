package com.hostelbooking.hostel_booking_backend.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hostelbooking.hostel_booking_backend.model.Hostel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class HostelService {
    private final Firestore db = FirestoreClient.getFirestore();

    public Hostel addHostel(Hostel hostel) throws ExecutionException, InterruptedException {
        String customHostelId = "HSTL-" + UUID.randomUUID().toString().substring(0, 8);
        hostel.setId(customHostelId);
        hostel.setApproved(false);
        DocumentReference docRef = db.collection("hostels").document(customHostelId);
        docRef.set(hostel).get();
        return hostel;
    }

    public Hostel updateHostel(String id, Hostel updatedHostel) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("hostels").document(id);
        DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            return null;
        }
        docRef.set(updatedHostel, SetOptions.merge()).get();
        return docRef.get().get().toObject(Hostel.class);
    }

    public List<Hostel> getAllHostels() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("hostels").whereEqualTo("approved", true).get().get();
        return query.toObjects(Hostel.class);
    }

    public Hostel getHostelById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("hostels").document(id).get().get();
        return doc.exists() ? doc.toObject(Hostel.class) : null;
    }

    public Hostel approveHostel(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("hostels").document(id);
        DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            return null;
        }
        Hostel hostel = doc.toObject(Hostel.class);
        hostel.setApproved(true);
        docRef.set(hostel).get();
        return hostel;
    }
}