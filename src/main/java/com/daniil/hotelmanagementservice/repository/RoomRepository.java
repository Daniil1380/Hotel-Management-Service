package com.daniil.hotelmanagementservice.repository;


import com.daniil.hotelmanagementservice.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.daniil.hotelmanagementservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailableTrue();

    List<Room> findByHotelId(Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.available = true AND r.tempLocked = false ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAndLockAvailableRooms();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Room> findById(Long id);

    long countByAvailableFalse();
}




