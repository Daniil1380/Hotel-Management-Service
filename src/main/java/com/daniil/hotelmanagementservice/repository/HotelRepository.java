package com.daniil.hotelmanagementservice.repository;


import com.daniil.hotelmanagementservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}

