package com.daniil.hotelmanagementservice.service;

import static org.junit.jupiter.api.Assertions.*;

import com.daniil.hotelmanagementservice.entity.Hotel;
import com.daniil.hotelmanagementservice.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel testHotel1;
    private Hotel testHotel2;

    @BeforeEach
    void setUp() {
        testHotel1 = new Hotel();
        testHotel1.setId(1L);
        testHotel1.setName("Grand Hotel");
        testHotel1.setAddress("123 Main St");

        testHotel2 = new Hotel();
        testHotel2.setId(2L);
        testHotel2.setName("Beach Resort");
        testHotel2.setAddress("456 Ocean Ave");
    }

    @Test
    void getAllHotels_WhenHotelsExist_ReturnsAllHotels() {
        // Arrange
        List<Hotel> expectedHotels = Arrays.asList(testHotel1, testHotel2);
        when(hotelRepository.findAll()).thenReturn(expectedHotels);

        // Act
        List<Hotel> actualHotels = hotelService.getAllHotels();

        // Assert
        assertEquals(2, actualHotels.size());
        assertEquals("Grand Hotel", actualHotels.get(0).getName());
        assertEquals("Beach Resort", actualHotels.get(1).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getAllHotels_WhenNoHotelsExist_ReturnsEmptyList() {
        // Arrange
        when(hotelRepository.findAll()).thenReturn(List.of());

        // Act
        List<Hotel> actualHotels = hotelService.getAllHotels();

        // Assert
        assertTrue(actualHotels.isEmpty());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void createHotel_WithValidHotel_ReturnsSavedHotel() {
        // Arrange
        Hotel newHotel = new Hotel();
        newHotel.setName("New Hotel");
        newHotel.setAddress("789 Park Blvd");

        when(hotelRepository.save(any(Hotel.class))).thenReturn(newHotel);

        // Act
        Hotel savedHotel = hotelService.createHotel(newHotel);

        // Assert
        assertNotNull(savedHotel);
        assertEquals("New Hotel", savedHotel.getName());
        assertEquals("789 Park Blvd", savedHotel.getAddress());
        verify(hotelRepository, times(1)).save(newHotel);
    }

    @Test
    void createHotel_WithNullFields_ThrowsException() {
        // Arrange
        Hotel invalidHotel = new Hotel();
        invalidHotel.setName(null);
        invalidHotel.setAddress(null);

        when(hotelRepository.save(any(Hotel.class))).thenThrow(new IllegalArgumentException());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hotelService.createHotel(invalidHotel);
        });
    }

    @Test
    void createHotel_WithDuplicateName_ThrowsException() {
        // Arrange
        Hotel duplicateHotel = new Hotel();
        duplicateHotel.setName("Grand Hotel"); // same as testHotel1
        duplicateHotel.setAddress("Different Address");

        when(hotelRepository.save(any(Hotel.class))).thenThrow(new RuntimeException("Duplicate hotel name"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            hotelService.createHotel(duplicateHotel);
        });
    }

    @Test
    void createHotel_VerifyRepositoryInteraction() {
        // Arrange
        Hotel newHotel = new Hotel();
        newHotel.setName("Test Hotel");
        newHotel.setAddress("Test Address");

        when(hotelRepository.save(any(Hotel.class))).thenReturn(newHotel);

        // Act
        hotelService.createHotel(newHotel);

        // Assert
        verify(hotelRepository, times(1)).save(newHotel);
        verifyNoMoreInteractions(hotelRepository);
    }

    @Test
    void getAllHotels_VerifyRepositoryInteraction() {
        // Arrange
        when(hotelRepository.findAll()).thenReturn(List.of(testHotel1));

        // Act
        hotelService.getAllHotels();

        // Assert
        verify(hotelRepository, times(1)).findAll();
        verifyNoMoreInteractions(hotelRepository);
    }
}