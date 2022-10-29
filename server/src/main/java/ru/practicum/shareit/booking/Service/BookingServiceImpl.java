package ru.practicum.shareit.booking.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.FromSizeRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingReturnDto;
import ru.practicum.shareit.exceptions.EntityNotAvailableException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    public BookingReturnDto addBooking(BookingDto bookingDto, Long userId) {
        Item item = itemService.getItemById(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new EntityNotAvailableException("Предмет недоступен");
        }
        if (item.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Невозможно забронировать свой предмет");
        }
        UserDto booker = userService.getUserById(userId);
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(UserMapper.toUser(booker));
        userService.getUserById(userId);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingReturnDto(bookingRepository.save(booking));
    }

    @Override
    public BookingReturnDto patchBooking(Long bookingId, Long userId, boolean approved) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new EntityNotFoundException("Бронирование не найдено");
        }
        Booking booking = optionalBooking.get();
        if (approved) {
            if (!booking.getItem().getUserId().equals(userId)) {
                throw new EntityNotFoundException("статус бронирования может менять только владелец вещи");
            }
            if (booking.getStatus().equals(BookingStatus.APPROVED)) {
                throw new IllegalStateException("Бронирование уже подтверждено");
            }
            booking.setStatus(BookingStatus.APPROVED);
        }
        if (booking.getItem().getUserId().equals(userId) && !approved) {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.toBookingReturnDto(bookingRepository.save(booking));
    }

    @Override
    public BookingReturnDto getBooking(Long bookingId, Long userId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new EntityNotFoundException("Бронирование не найдено");
        }
        Booking booking = optionalBooking.get();

        if (!booking.getItem().getUserId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new EntityNotFoundException("Пользователь не является владельцем вещи или автором бронирования");
        }
        return BookingMapper.toBookingReturnDto(optionalBooking.get());
    }

    @Override
    public List<BookingReturnDto> getUserBookingList(Long userId, String state, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = FromSizeRequest.of(from, size);
        switch (state) {
            case "FUTURE":
                return bookingRepository.findFutureBookingsByBooker(userId, LocalDateTime.now(), pageable)
                        .stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findBookingsByBooker(userId, pageable).stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findBookingsByBookerAndStatus(userId, BookingStatus.WAITING, pageable).stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findBookingsByBookerAndStatus(userId, BookingStatus.REJECTED, pageable).stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findCurrentBookingForUser(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findPastBookingForUser(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            default:
                throw new EntityNotAvailableException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingReturnDto> getOwnerBookingList(Long userId, String state, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = FromSizeRequest.of(from, size);
        Stream<Long> itemsId = itemService.getItems(userId, 0, Integer.MAX_VALUE).map(ItemDto::getId);
        switch (state) {
            case "FUTURE":
                return itemsId.flatMap((id) ->
                                bookingRepository.findFutureBookingsByItem(
                                        id, LocalDateTime.now(), pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());

            case "ALL":
                return itemsId.flatMap((id) -> bookingRepository.findBookingsByItem(id, pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return itemsId.flatMap((id) -> bookingRepository
                                .findBookingsByItemAndStatus(id, BookingStatus.WAITING, pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return itemsId.flatMap((id) -> bookingRepository
                                .findBookingsByItemAndStatus(id, BookingStatus.REJECTED, pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return itemsId.flatMap((id) -> bookingRepository
                                .findCurrentBookingForOwner(id, LocalDateTime.now(), pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            case "PAST":
                return itemsId.flatMap((id) -> bookingRepository
                                .findPastBookingForOwner(id, LocalDateTime.now(), pageable).stream())
                        .map(BookingMapper::toBookingReturnDto)
                        .collect(Collectors.toList());
            default:
                throw new EntityNotAvailableException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
