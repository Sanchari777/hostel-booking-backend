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

    // Add a hostel with a custom ID
    public Hostel addHostel(Hostel hostel) throws ExecutionException, InterruptedException {
        String customHostelId = "HSTL-" + UUID.randomUUID().toString().substring(0, 8);
        hostel.setId(customHostelId);
        DocumentReference docRef = db.collection("hostels").document(customHostelId);
        docRef.set(hostel).get();
        return hostel;
    }

    // Update an existing hostel
    public Hostel updateHostel(String id, Hostel updatedHostel) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("hostels").document(id);
        DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            return null;
        }

        Hostel existingHostel = doc.toObject(Hostel.class);
        if (updatedHostel.getName() == null) updatedHostel.setName(existingHostel.getName());
        if (updatedHostel.getEmail() == null) updatedHostel.setEmail(existingHostel.getEmail());
        if (updatedHostel.getPhone() == null) updatedHostel.setPhone(existingHostel.getPhone());
        if (updatedHostel.getOwnerName() == null) updatedHostel.setOwnerName(existingHostel.getOwnerName());
        if (updatedHostel.getAddress() == null) updatedHostel.setAddress(existingHostel.getAddress());
        if (updatedHostel.getPinCode() == null) updatedHostel.setPinCode(existingHostel.getPinCode());
        if (updatedHostel.getSpecification() == null) updatedHostel.setSpecification(existingHostel.getSpecification());
        if (updatedHostel.getStayingOptions() == null) updatedHostel.setStayingOptions(existingHostel.getStayingOptions());
        if (updatedHostel.getPricePerDay() == null) updatedHostel.setPricePerDay(existingHostel.getPricePerDay());
        if (updatedHostel.getPricePerWeek() == null) updatedHostel.setPricePerWeek(existingHostel.getPricePerWeek());
        if (updatedHostel.getPricePerMonth() == null) updatedHostel.setPricePerMonth(existingHostel.getPricePerMonth());
        if (updatedHostel.getFacilities() == null) updatedHostel.setFacilities(existingHostel.getFacilities());
        if (updatedHostel.getAvailableRooms() == null) updatedHostel.setAvailableRooms(existingHostel.getAvailableRooms());
        if (updatedHostel.getAvailableBeds() == null) updatedHostel.setAvailableBeds(existingHostel.getAvailableBeds());
        if (updatedHostel.getImageUrls() == null) updatedHostel.setImageUrls(existingHostel.getImageUrls());
        if (updatedHostel.getRulesAndPolicies() == null) updatedHostel.setRulesAndPolicies(existingHostel.getRulesAndPolicies());
        updatedHostel.setId(id);

        docRef.set(updatedHostel, SetOptions.merge()).get();
        return docRef.get().get().toObject(Hostel.class);
    }

    // Get all hostels
    public List<Hostel> getAllHostels() throws ExecutionException, InterruptedException {
        QuerySnapshot query = db.collection("hostels").get().get();
        return query.toObjects(Hostel.class);
    }

    // Get a hostel by ID
    public Hostel getHostelById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("hostels").document(id).get().get();
        return doc.exists() ? doc.toObject(Hostel.class) : null;
    }

    // Approve a hostel
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