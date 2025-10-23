package com.daniil.hotelmanagementservice.service;

import com.daniil.hotelmanagementservice.entity.Hotel;
import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.HotelRepository;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setNumber("101");
        testRoom.setAvailable(true);
        testRoom.setTempLocked(false);
        testRoom.setTimesBooked(5);
        testRoom.setHotelId(testHotel.getId());
    }

    @Test
    void allocateRoom_WhenRoomsAvailable_ReturnsRoomAndLocksIt() {
        // Arrange
        when(roomRepository.findAndLockAvailableRooms()).thenReturn(List.of(testRoom));

        // Act
        Optional<Room> result = roomService.allocateRoom();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRoom, result.get());
        assertTrue(testRoom.isTempLocked());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void allocateRoom_WhenNoRoomsAvailable_ReturnsEmpty() {
        // Arrange
        when(roomRepository.findAndLockAvailableRooms()).thenReturn(Collections.emptyList());

        // Act
        Optional<Room> result = roomService.allocateRoom();

        // Assert
        assertFalse(result.isPresent());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void confirmBooking_WhenRoomExists_IncrementsTimesBookedAndUnlocks() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // Act
        roomService.confirmBooking(1L);

        // Assert
        assertEquals(6, testRoom.getTimesBooked());
        assertFalse(testRoom.isTempLocked());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void confirmBooking_WhenRoomNotExists_DoesNothing() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        roomService.confirmBooking(1L);

        // Assert
        verify(roomRepository, never()).save(any());
    }

    @Test
    void releaseRoom_WhenRoomExists_UnlocksRoom() {
        // Arrange
        testRoom.setTempLocked(true);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // Act
        roomService.releaseRoom(1L);

        // Assert
        assertFalse(testRoom.isTempLocked());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void releaseRoom_WhenRoomNotExists_DoesNothing() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        roomService.releaseRoom(1L);

        // Assert
        verify(roomRepository, never()).save(any());
    }

    @Test
    void getAllAvailableRooms_ReturnsOnlyAvailableRooms() {
        // Arrange
        Room unavailableRoom = new Room();
        unavailableRoom.setAvailable(false);
        when(roomRepository.findByAvailableTrue()).thenReturn(List.of(testRoom));

        // Act
        List<Room> result = roomService.getAllAvailableRooms();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRoom, result.get(0));
    }

    @Test
    void getOccupancyStats_ReturnsCorrectStats() {
        // Arrange
        when(roomRepository.count()).thenReturn(10L);
        when(roomRepository.countByAvailableFalse()).thenReturn(4L);
        when(hotelRepository.findAll()).thenReturn(List.of(testHotel));
        when(roomRepository.findByHotelId(testHotel.getId())).thenReturn(List.of(testRoom));

        // Act
        Map<String, Object> stats = roomService.getOccupancyStats();

        // Assert
        assertEquals(10L, stats.get("totalRooms"));
        assertEquals(6L, stats.get("availableRooms"));
        assertEquals(4L, stats.get("occupiedRooms"));

        @SuppressWarnings("unchecked")
        Map<String, Object> hotelStats = (Map<String, Object>) stats.get("byHotel");
        assertEquals(1, hotelStats.size());
        assertTrue(hotelStats.containsKey("Test Hotel"));
    }


    @Test
    void searchRooms_WithNullFilters_ReturnsAllRooms() {
        // Arrange
        when(roomRepository.findAll()).thenReturn(List.of(testRoom));

        // Act
        List<Room> result = roomService.searchRooms(
                null,   // hotelId
                null,   // available
                null,   // numberPattern
                0,      // minTimesBooked
                100,    // maxTimesBooked
                "id"    // sortBy
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRoom, result.get(0));
    }


    @Test
    void createRoom_SavesAndReturnsRoom() {
        // Arrange
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // Act
        Room result = roomService.createRoom(testRoom);

        // Assert
        assertEquals(testRoom, result);
        verify(roomRepository).save(testRoom);
    }

    @Test
    void getOccupancyStats_WhenNoRooms_ReturnsZeroRates() {
        // Arrange
        when(roomRepository.count()).thenReturn(0L);
        when(roomRepository.countByAvailableFalse()).thenReturn(0L);
        when(hotelRepository.findAll()).thenReturn(List.of(testHotel));
        when(roomRepository.findByHotelId(testHotel.getId())).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = roomService.getOccupancyStats();

        // Assert
        assertEquals(0L, stats.get("totalRooms"));
        assertEquals(0L, stats.get("availableRooms"));
        assertEquals(0L, stats.get("occupiedRooms"));
    }
}