package com.daniil.hotelmanagementservice.repository;


import com.daniil.hotelmanagementservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByCity(String city);
    List<Hotel> findByCountry(String country);
}

