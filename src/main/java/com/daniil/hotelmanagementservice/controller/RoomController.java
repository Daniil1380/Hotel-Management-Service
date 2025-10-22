package com.daniil.hotelmanagementservice.controller;
import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import com.daniil.hotelmanagementservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomRepository roomRepository;

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
        return roomRepository.save(room);
    }
}



