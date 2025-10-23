package com.daniil.hotelmanagementservice.config;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.boot.CommandLineRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private DataInit dataInit;

    @Test
    void initDatabase_WhenDatabaseEmpty_CreatesHotelsAndRooms() throws Exception {
        // Arrange
        when(hotelRepository.count()).thenReturn(0L);
        when(roomRepository.count()).thenReturn(0L);

        Hotel savedHotel1 = Hotel.builder().id(1L).name("Aurora Hotel").address("Amsterdam").build();
        Hotel savedHotel2 = Hotel.builder().id(2L).name("Sea Breeze Resort").address("Rotterdam").build();

        when(hotelRepository.save(any(Hotel.class)))
                .thenReturn(savedHotel1)
                .thenReturn(savedHotel2);

        when(roomRepository.save(any(Room.class))).thenReturn(new Room());

        // Act
        CommandLineRunner runner = dataInit.initDatabase(hotelRepository, roomRepository);
        runner.run();

        // Assert
        verify(hotelRepository, times(2)).save(any(Hotel.class));
        verify(roomRepository, times(4)).save(any(Room.class));
    }

    @Test
    void initDatabase_VerifyHotelProperties() throws Exception {
        // Arrange
        when(hotelRepository.count()).thenReturn(0L);
        when(roomRepository.count()).thenReturn(0L);

        Hotel savedHotel1 = Hotel.builder().id(1L).name("Aurora Hotel").address("Amsterdam").build();
        Hotel savedHotel2 = Hotel.builder().id(2L).name("Sea Breeze Resort").address("Rotterdam").build();

        when(hotelRepository.save(any(Hotel.class)))
                .thenReturn(savedHotel1)
                .thenReturn(savedHotel2);

        // Act
        CommandLineRunner runner = dataInit.initDatabase(hotelRepository, roomRepository);
        runner.run();

        // Assert
        verify(hotelRepository).save(argThat(hotel ->
                hotel.getName().equals("Aurora Hotel") &&
                        hotel.getAddress().equals("Amsterdam")
        ));
        verify(hotelRepository).save(argThat(hotel ->
                hotel.getName().equals("Sea Breeze Resort") &&
                        hotel.getAddress().equals("Rotterdam")
        ));
    }

}