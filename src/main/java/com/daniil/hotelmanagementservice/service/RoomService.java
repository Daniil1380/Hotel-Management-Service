package com.daniil.hotelmanagementservice.service;


import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public List<Room> getRecommendedRooms() {
        return roomRepository.findByAvailableTrue().stream()
                .sorted(Comparator.comparingInt(Room::getTimesBooked)
                        .thenComparing(Room::getId))
                .toList();
    }

    @Transactional
    public boolean confirmAvailability(Long roomId) {
        return roomRepository.findById(roomId)
                .filter(Room::isAvailable)
                .filter(r -> !r.isTempLocked())
                .map(r -> {
                    r.setTempLocked(true);
                    roomRepository.save(r);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void releaseRoom(Long roomId) {
        roomRepository.findById(roomId).ifPresent(r -> {
            r.setTempLocked(false);
            roomRepository.save(r);
        });
    }

    @Transactional
    public void incrementTimesBooked(Long roomId) {
        roomRepository.findById(roomId).ifPresent(r -> {
            r.setTimesBooked(r.getTimesBooked() + 1);
            r.setTempLocked(false);
            roomRepository.save(r);
        });
    }
}

