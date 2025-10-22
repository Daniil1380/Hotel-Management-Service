package com.daniil.hotelmanagementservice.config;


import com.daniil.hotelmanagementservice.entity.Hotel;
import com.daniil.hotelmanagementservice.entity.Room;
import com.daniil.hotelmanagementservice.repository.HotelRepository;
import com.daniil.hotelmanagementservice.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Bean
    CommandLineRunner initDatabase(HotelRepository hotelRepository, RoomRepository roomRepository) {
        return args -> {
            if (hotelRepository.count() == 0 && roomRepository.count() == 0) {
                Hotel h1 = Hotel.builder()
                        .name("Aurora Hotel")
                        .address("Amsterdam")
                        .build();
                Hotel h2 = Hotel.builder()
                        .name("Sea Breeze Resort")
                        .address("Rotterdam")
                        .build();

                hotelRepository.save(h1);
                hotelRepository.save(h2);

                // после сохранения отелей получаем их ID
                Long h1Id = h1.getId();
                Long h2Id = h2.getId();

                roomRepository.save(Room.builder()
                        .hotelId(h1Id)
                        .number("101")
                        .available(true)
                        .timesBooked(0)
                        .tempLocked(false)
                        .build());
                roomRepository.save(Room.builder()
                        .hotelId(h1Id)
                        .number("102")
                        .available(true)
                        .timesBooked(0)
                        .tempLocked(false)
                        .build());
                roomRepository.save(Room.builder()
                        .hotelId(h2Id)
                        .number("201")
                        .available(true)
                        .timesBooked(0)
                        .tempLocked(false)
                        .build());
                roomRepository.save(Room.builder()
                        .hotelId(h2Id)
                        .number("202")
                        .available(true)
                        .timesBooked(0)
                        .tempLocked(false)
                        .build());

                System.out.println("✅ Hotels & rooms initialized!");
            }
        };
    }
}

