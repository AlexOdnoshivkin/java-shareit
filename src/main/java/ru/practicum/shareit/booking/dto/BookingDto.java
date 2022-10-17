package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.validation.EndNotInPast;
import ru.practicum.shareit.validation.StartNotInPast;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private BookingStatus status;
    private Long bookerId;
    private Long itemId;
    @StartNotInPast
    private LocalDateTime start;
    @EndNotInPast
    private LocalDateTime end;
    private String itemName;

}
