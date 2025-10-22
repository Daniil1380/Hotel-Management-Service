package com.daniil.hotelmanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private String number;
    private boolean available = true;
    private int timesBooked = 0;

    private boolean tempLocked = false; // для confirm-availability
}

