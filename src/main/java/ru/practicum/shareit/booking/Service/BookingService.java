package ru.practicum.shareit.booking.Service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReturnDto;

import java.util.List;

public interface BookingService {
    BookingReturnDto addBooking(BookingDto bookingDto, Long userId);

    BookingReturnDto patchBooking(Long bookingId, Long userId, boolean approved);

    BookingReturnDto getBooking(Long bookingId, Long userId);

    List<BookingReturnDto> getUserBookingList(Long userId, String state);

    List<BookingReturnDto> getOwnerBookingList(Long userId, String state);

}
