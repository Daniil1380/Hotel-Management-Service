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

    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Room> getAvailableRooms() {
        return roomService.getAllAvailableRooms();
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Room> getRecommendedRooms() {
        return roomService.getRecommendedRooms();
    }

    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public boolean confirmAvailability(@PathVariable Long id) {
        return roomService.confirmAvailability(id);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void releaseRoom(@PathVariable Long id) {
        roomService.releaseRoom(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }
}

