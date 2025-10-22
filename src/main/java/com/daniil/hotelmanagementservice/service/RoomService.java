package com.daniil.hotelmanagementservice.service;


import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public Optional<Room> allocateRoom() {
        var availableRooms = roomRepository.findAndLockAvailableRooms();
        if (availableRooms.isEmpty()) {
            log.warn("No available rooms found!");
            return Optional.empty();
        }
        Room room = availableRooms.get(0);
        room.setTempLocked(true);
        roomRepository.save(room);
        log.info("Room {} allocated (timesBooked={}, available={})", room.getId(), room.getTimesBooked(), room.isAvailable());
        return Optional.of(room);
    }

    @Transactional
    public void confirmBooking(Long roomId) {
        roomRepository.findById(roomId).ifPresent(room -> {
            room.setTimesBooked(room.getTimesBooked() + 1);
            room.setTempLocked(false);
            roomRepository.save(room);
            log.info("Room {} confirmed, timesBooked={}", roomId, room.getTimesBooked());
        });
    }

    @Transactional
    public void releaseRoom(Long roomId) {
        roomRepository.findById(roomId).ifPresent(room -> {
            room.setTempLocked(false);
            roomRepository.save(room);
            log.info("Room {} released", roomId);
        });
    }

    @Transactional(readOnly = true)
    public List<Room> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }
}


