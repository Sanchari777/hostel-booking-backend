//package com.hostelbooking.hostel_booking_backend.repository;
//
//
//import com.hostelbooking.hostel_booking_backend.model.Hostel;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface HostelRepository extends JpaRepository<Hostel, Long> {
//    List<Hostel> findByOwnerId(Long ownerId);
//    List<Hostel> findByApprovedFalse();
//}