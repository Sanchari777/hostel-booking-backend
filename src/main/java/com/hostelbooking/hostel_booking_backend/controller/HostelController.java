package com.hostelbooking.hostel_booking_backend.controller;

import com.hostelbooking.hostel_booking_backend.model.Hostel;
import com.hostelbooking.hostel_booking_backend.service.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hostels")
@Validated
public class HostelController {

    @Autowired
    private HostelService hostelService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendHostelOtp(@Valid @RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String deliveryMethod = request.get("deliveryMethod");
            String message = hostelService.sendHostelOtp(phone, deliveryMethod);
            return ResponseEntity.ok(new SuccessResponse(message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHostel(@Valid @RequestBody Map<String, Object> request) {
        try {
            Hostel hostel = new Hostel();
            @SuppressWarnings("unchecked")
            Map<String, Object> hostelData = (Map<String, Object>) request.get("hostel");
            hostel.setName((String) hostelData.get("name"));
            hostel.setPhone((String) hostelData.get("phone"));
            hostel.setEmail((String) hostelData.get("email"));
            hostel.setOwnerName((String) hostelData.get("ownerName"));
            hostel.setAddress((String) hostelData.get("address"));
            hostel.setPinCode((String) hostelData.get("pinCode"));
            hostel.setSpecification((String) hostelData.get("specification"));
            hostel.setStayingOptions((List<String>) hostelData.get("stayingOptions"));
            hostel.setPricePerDay(((Number) hostelData.get("pricePerDay")).doubleValue());
            hostel.setPricePerWeek(((Number) hostelData.get("pricePerWeek")).doubleValue());
            hostel.setPricePerMonth(((Number) hostelData.get("pricePerMonth")).doubleValue());
            hostel.setFacilities((List<String>) hostelData.get("facilities"));
            hostel.setAvailableRooms(((Number) hostelData.get("availableRooms")).intValue());
            hostel.setAvailableBeds(((Number) hostelData.get("availableBeds")).intValue());
            hostel.setImageUrls((List<String>) hostelData.get("imageUrls"));
            hostel.setRulesAndPolicies((String) hostelData.get("rulesAndPolicies"));
            String otp = (String) request.get("otp");
            Hostel registeredHostel = hostelService.registerHostel(hostel, otp);
            return ResponseEntity.ok(registeredHostel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<?> sendHostelLoginOtp(@RequestBody Map<String, String> request) {
        try {
            String id = request.get("id");
            String phone = request.get("phone");
            String deliveryMethod = request.get("deliveryMethod");
            String message = hostelService.sendHostelLoginOtp(id, phone, deliveryMethod);
            return ResponseEntity.ok(new SuccessResponse(message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginHostel(@RequestBody Map<String, String> request) {
        try {
            String id = request.get("id");
            String phone = request.get("phone");
            String otp = request.get("otp");
            Hostel hostel = hostelService.loginHostel(id, phone, otp);
            return ResponseEntity.ok(hostel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHostel(@PathVariable String id, @Valid @RequestBody Hostel hostel) {
        try {
            Hostel updatedHostel = hostelService.updateHostel(id, hostel);
            return ResponseEntity.ok(updatedHostel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Hostel>> getPendingHostels() {
        return ResponseEntity.ok(hostelService.getPendingHostels());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveHostel(@PathVariable String id) {
        try {
            Hostel hostel = hostelService.approveHostel(id);
            return ResponseEntity.ok(hostel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHostelById(@PathVariable String id) {
        try {
            Hostel hostel = hostelService.getHostelById(id);
            return ResponseEntity.ok(hostel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Hostel>> getHostels() {
        return ResponseEntity.ok(hostelService.getHostels());
    }
}