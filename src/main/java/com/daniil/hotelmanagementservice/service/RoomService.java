package com.daniil.hotelmanagementservice.service;


import com.daniil.hotelmanagementservice.entity.Hotel;
import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.HotelRepository;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

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

    @Transactional(readOnly = true)
    public Map<String, Object> getOccupancyStats() {
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByAvailableFalse();
        long availableRooms = totalRooms - occupiedRooms;

        double occupancyRate = totalRooms == 0 ? 0 : ((double) occupiedRooms / totalRooms) * 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", totalRooms);
        stats.put("availableRooms", availableRooms);
        stats.put("occupiedRooms", occupiedRooms);
        stats.put("occupancyRate", String.format("%.2f%%", occupancyRate));

        // Расширенная: по отелям
        Map<String, Object> hotelStats = hotelRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Hotel::getName,
                        hotel -> {
                            List<Room> rooms = roomRepository.findByHotelId(hotel.getId());
                            long total = rooms.size();
                            long occupied = rooms.stream().filter(r -> !r.isAvailable()).count();
                            return Map.of(
                                    "total", total,
                                    "occupied", occupied,
                                    "rate", total == 0 ? 0 : ((double) occupied / total) * 100
                            );
                        }
                ));

        stats.put("byHotel", hotelStats);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Room> searchRooms(Long hotelId, Boolean available, String numberPattern, int minTimesBooked, int maxTimesBooked, String sortBy) {
        List<Room> rooms = roomRepository.findAll();

        // Фильтрация
        rooms = rooms.stream()
                .filter(room -> hotelId == null || room.getHotelId().equals(hotelId))
                .filter(room -> available == null || room.isAvailable() == available)
                .filter(room -> numberPattern == null || room.getNumber().contains(numberPattern))
                .filter(room -> room.getTimesBooked() >= minTimesBooked)
                .filter(room -> room.getTimesBooked() <= maxTimesBooked)
                .collect(Collectors.toList());

        // Сортировка
        switch (sortBy) {
            case "timesBooked_desc":
                rooms.sort(Comparator.comparing(Room::getTimesBooked).reversed());
                break;
            case "number":
                rooms.sort(Comparator.comparing(Room::getNumber));
                break;
            default:
                rooms.sort(Comparator.comparing(Room::getId));
        }

        return rooms;
    }

    @Transactional
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }
}


