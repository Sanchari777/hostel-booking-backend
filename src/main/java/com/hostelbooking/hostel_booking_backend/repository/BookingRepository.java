//package com.hostelbooking.hostel_booking_backend.repository;
//
//
//import com.hostelbooking.hostel_booking_backend.model.Booking;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface BookingRepository extends JpaRepository<Booking, Long> {
//    List<Booking> findByUserId(Long userId);
//    List<Booking> findByHostelId(Long hostelId);
//}