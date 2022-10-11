package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.Service.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReturnDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingReturnDto addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @Valid @RequestBody BookingDto bookingDto) {
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
    public BookingReturnDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userID, @PathVariable Long bookingId) {
        log.info("Get-запрос на получение бронирования. id бронирования {}, id ползователя {}", bookingId, userID);
        return bookingService.getBooking(bookingId, userID);
    }

    @GetMapping
    public List<BookingReturnDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userID,
                                                  @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.info("Get-запрос на получение чпичка бронирований пользователя с id {} и статусом {}", userID, state);
        return bookingService.getUserBookingList(userID, state);
    }

    @GetMapping("/owner")
    public List<BookingReturnDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userID,
                                                   @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.info("Get-запрос на получение чпичка бронирований пользователя с id {} и статусом {}", userID, state);
        return bookingService.getOwnerBookingList(userID, state);
    }

}
