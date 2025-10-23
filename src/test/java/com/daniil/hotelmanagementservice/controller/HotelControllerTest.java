package com.daniil.hotelmanagementservice.controller;

import static org.junit.jupiter.api.Assertions.*;


import com.daniil.hotelmanagementservice.entity.Hotel;
import com.daniil.hotelmanagementservice.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelControllerTest {

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelController hotelController;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");

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
    void getAllHotels_ReturnsListOfHotels() {
        // Arrange
        when(hotelService.getAllHotels()).thenReturn(List.of(testHotel));

        // Act
        List<Hotel> result = hotelController.getAllHotels();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testHotel, result.get(0));
        verify(hotelService).getAllHotels();
    }

    @Test
    void getAllHotels_WhenNoHotels_ReturnsEmptyList() {
        // Arrange
        when(hotelService.getAllHotels()).thenReturn(List.of());

        // Act
        List<Hotel> result = hotelController.getAllHotels();

        // Assert
        assertTrue(result.isEmpty());
        verify(hotelService).getAllHotels();
    }

    @Test
    void createHotel_WithValidHotel_ReturnsCreatedHotel() {
        // Arrange
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(testHotel);

        // Act
        Hotel result = hotelController.createHotel(testHotel);

        // Assert
        assertNotNull(result);
        assertEquals(testHotel, result);
        verify(hotelService).createHotel(testHotel);
    }

    @Test
    void createHotel_WithUserRole_Success() {
        // Arrange (уже установлена роль USER в setUp)
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(testHotel);

        // Act
        Hotel result = hotelController.createHotel(testHotel);

        // Assert
        assertNotNull(result);
    }

    @Test
    void createHotel_WithAdminRole_Success() {
        // Arrange
        setupSecurityContext("ROLE_ADMIN");
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(testHotel);

        // Act
        Hotel result = hotelController.createHotel(testHotel);

        // Assert
        assertNotNull(result);
    }

    @Test
    void createHotel_VerifyServiceInteraction() {
        // Arrange
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(testHotel);

        // Act
        hotelController.createHotel(testHotel);

        // Assert
        verify(hotelService, times(1)).createHotel(testHotel);
        verifyNoMoreInteractions(hotelService);
    }

    @Test
    void getAllHotels_VerifyServiceInteraction() {
        // Arrange
        when(hotelService.getAllHotels()).thenReturn(List.of(testHotel));

        // Act
        hotelController.getAllHotels();

        // Assert
        verify(hotelService, times(1)).getAllHotels();
        verifyNoMoreInteractions(hotelService);
    }
}