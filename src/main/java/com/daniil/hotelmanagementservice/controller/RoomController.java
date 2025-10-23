package com.daniil.hotelmanagementservice.controller;
import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import com.daniil.hotelmanagementservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Получить все доступные комнаты (информационный endpoint)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Room> getAvailableRooms() {
        return roomService.getAllAvailableRooms();
    }

    /**
     * Алгоритм планирования: выбирает оптимальный номер (с наименьшим timesBooked)
     * и временно блокирует его.
     */
    @GetMapping("/allocate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Long allocateRoom() {
        return roomService.allocateRoom()
                .map(Room::getId)
                .orElse(null);
    }

    /**
     * Подтверждение бронирования — увеличивает счётчик timesBooked
     * и снимает временную блокировку.
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void confirmBooking(@PathVariable Long id) {
        roomService.confirmBooking(id);
    }

    /**
     * Освободить комнату (при отмене бронирования или неудаче)
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void releaseRoom(@PathVariable Long id) {
        roomService.releaseRoom(id);
    }

    /**
     * Создание новой комнаты (только админ)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Room createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @GetMapping("/stats/searchRooms")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Room> searchRooms(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String number,
            @RequestParam(defaultValue = "0") int minBooked,
            @RequestParam(defaultValue = "1000") int maxBooked,
            @RequestParam(defaultValue = "id") String sort) {

        return roomService.searchRooms(hotelId, available, number, minBooked, maxBooked, sort);
    }

    @GetMapping("/stats/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStats() {
        return roomService.getOccupancyStats();
    }
}



