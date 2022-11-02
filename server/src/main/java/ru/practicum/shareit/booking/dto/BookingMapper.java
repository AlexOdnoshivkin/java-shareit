package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

public class BookingMapper {
    private BookingMapper() {
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return new Booking(bookingDto.getId(),
                bookingDto.getStatus(),
                null,
                null,
                bookingDto.getStart(),
                bookingDto.getEnd());
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(booking.getId(),
                booking.getStatus(),
                booking.getBooker().getId(),
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getName());
    }

    public static BookingReturnDto toBookingReturnDto(Booking booking) {
        return new BookingReturnDto(booking.getId(),
                booking.getStatus(),
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem()),
                booking.getStart(),
                booking.getEnd());
    }
}
