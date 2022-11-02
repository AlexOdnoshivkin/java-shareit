package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.Service.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReturnDto;


import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingReturnDto addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestBody BookingDto bookingDto) {
        log.info("Post-запрос на добавление бронирования {}", bookingDto);
        return bookingService.addBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingReturnDto patchBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable Long bookingId, @RequestParam boolean approved) {
        log.info("Patch-запрос на изменение бронирования. id бронирования {}, статус {}", bookingId, approved);
        return bookingService.patchBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingReturnDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Get-запрос на получение бронирования. id бронирования {}, id ползователя {}", bookingId, userId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingReturnDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(required = false, defaultValue = "ALL") String state,
                                                  @RequestParam(name = "from", defaultValue = "0")
                                                      Integer from,
                                                  @RequestParam(name = "size", defaultValue = "10")
                                                  Integer size) {
        log.info("Get-запрос на получение cпиcка бронирований пользователя с id {} и статусом {}", userId, state);
        return bookingService.getUserBookingList(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingReturnDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(required = false, defaultValue = "ALL") String state,
                                                   @RequestParam(name = "from", defaultValue = "0")
                                                       Integer from,
                                                   @RequestParam(name = "size", defaultValue = "10")
                                                   Integer size) {
        log.info("Get-запрос на получение cпиcка бронирований пользователя с id {} и статусом {}", userId, state);
        return bookingService.getOwnerBookingList(userId, state, from, size);
    }

}
