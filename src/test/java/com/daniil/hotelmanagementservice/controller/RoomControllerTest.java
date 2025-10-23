package com.daniil.hotelmanagementservice.controller;

import static org.junit.jupiter.api.Assertions.*;


import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setNumber("101");
        testRoom.setAvailable(true);

        // Настройка security context для USER
        setupSecurityContext("ROLE_USER");
    }

    private void setupSecurityContext(String... roles) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(roles[0])
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void getAvailableRooms_ReturnsListOfRooms() {
        // Arrange
        when(roomService.getAllAvailableRooms()).thenReturn(List.of(testRoom));

        // Act
        List<Room> result = roomController.getAvailableRooms();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRoom, result.get(0));
        verify(roomService).getAllAvailableRooms();
    }

    @Test
    void allocateRoom_WhenRoomAvailable_ReturnsRoomId() {
        // Arrange
        when(roomService.allocateRoom()).thenReturn(Optional.of(testRoom));

        // Act
        Long result = roomController.allocateRoom();

        // Assert
        assertEquals(1L, result);
        verify(roomService).allocateRoom();
    }

    @Test
    void allocateRoom_WhenNoRoomsAvailable_ReturnsNull() {
        // Arrange
        when(roomService.allocateRoom()).thenReturn(Optional.empty());

        // Act
        Long result = roomController.allocateRoom();

        // Assert
        assertNull(result);
        verify(roomService).allocateRoom();
    }

    @Test
    void confirmBooking_CallsServiceWithCorrectId() {
        // Arrange
        doNothing().when(roomService).confirmBooking(any(Long.class));

        // Act
        roomController.confirmBooking(1L);

        // Assert
        verify(roomService).confirmBooking(1L);
    }

    @Test
    void releaseRoom_CallsServiceWithCorrectId() {
        // Arrange
        doNothing().when(roomService).releaseRoom(any(Long.class));

        // Act
        roomController.releaseRoom(1L);

        // Assert
        verify(roomService).releaseRoom(1L);
    }

    @Test
    void createRoom_WithAdminRole_ReturnsCreatedRoom() {
        // Arrange
        setupSecurityContext("ROLE_ADMIN");
        when(roomService.createRoom(any(Room.class))).thenReturn(testRoom);

        // Act
        Room result = roomController.createRoom(testRoom);

        // Assert
        assertEquals(testRoom, result);
        verify(roomService).createRoom(testRoom);
    }

    @Test
    void searchRooms_WithAllParameters_ReturnsFilteredRooms() {
        // Arrange
        when(roomService.searchRooms(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of(testRoom));

        // Act
        List<Room> result = roomController.searchRooms(1L, true, "101", 0, 10, "id");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRoom, result.get(0));
        verify(roomService).searchRooms(1L, true, "101", 0, 10, "id");
    }

    @Test
    void searchRooms_WithDefaultParameters_ReturnsAllRooms() {
        // Arrange
        when(roomService.searchRooms(null, null, null, 0, 1000, "id"))
                .thenReturn(List.of(testRoom));

        // Act
        List<Room> result = roomController.searchRooms(null, null, null, 0, 1000, "id");

        // Assert
        assertEquals(1, result.size());
        verify(roomService).searchRooms(null, null, null, 0, 1000, "id");
    }

    @Test
    void getStats_ReturnsOccupancyStats() {
        // Arrange
        Map<String, Object> expectedStats = Map.of(
                "totalRooms", 10,
                "availableRooms", 5,
                "occupancyRate", "50.00%"
        );
        when(roomService.getOccupancyStats()).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = roomController.getStats();

        // Assert
        assertEquals(expectedStats, result);
        verify(roomService).getOccupancyStats();
    }


}