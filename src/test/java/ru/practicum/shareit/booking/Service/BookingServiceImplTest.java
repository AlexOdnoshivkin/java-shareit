package ru.practicum.shareit.booking.Service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingReturnDto;
import ru.practicum.shareit.exceptions.EntityNotAvailableException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final BookingService bookingService;
    private User user;
    private Item item;
    private Booking booking;

    private final EntityManager em;

    @BeforeEach
    void creteEntity() {
        user = new User();
        user.setName("TestUser");
        user.setEmail("Test@gmail.com");
        em.persist(user);


        item = new Item();
        item.setAvailable(true);
        item.setName("TestItem");
        item.setDescription("Test description");
        item.setUserId(user.getId());

        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(21));
        booking.setEnd(LocalDateTime.now().minusDays(10));
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table bookings");
    }

    @Test
    void addBooking() {
        // Проверка случая, когда предмет недоступен
        item.setAvailable(false);
        em.persist(item);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(21));
        bookingDto.setEnd(LocalDateTime.now().minusDays(10));

        try {
            bookingService.addBooking(bookingDto, user.getId());
        } catch (EntityNotAvailableException e) {
            assertEquals("Предмет недоступен", e.getMessage());
        }

        // Проверка попытки бронирования своего предмета
        item.setAvailable(true);
        item.setUserId(user.getId());
        em.persist(item);

        try {
            bookingService.addBooking(bookingDto, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Невозможно забронировать свой предмет", e.getMessage());
        }

        // Проверка корректного случая
        item.setUserId(100L);
        BookingReturnDto trueResult = new BookingReturnDto();
        trueResult.setStart(bookingDto.getStart());
        trueResult.setEnd(bookingDto.getEnd());
        trueResult.setItem(ItemMapper.toItemDto(item));
        trueResult.setBooker(UserMapper.toUserDto(user));
        trueResult.setStatus(BookingStatus.WAITING);

        BookingReturnDto result = bookingService.addBooking(bookingDto, user.getId());

        trueResult.setId(result.getId());

        assertNotNull(result);
        assertEquals(trueResult, result);

        // Проерка валидации бронирования
        bookingDto.setStart(LocalDateTime.now().plusDays(10));
        bookingDto.setEnd(LocalDateTime.now());

        try {
            bookingService.addBooking(bookingDto, user.getId());
        } catch (IllegalStateException e) {
            assertEquals("Дата начала бронирования не может быть позже даты завершения", e.getMessage());
        }
    }

    @Test
    void patchBooking() {
        // Проверка исключения, если бронирование не найдено
        try {
            bookingService.patchBooking(100L, user.getId(), true);
        } catch (EntityNotFoundException e) {
            assertEquals("Бронирование не найдено", e.getMessage());
        }

        // Проверка искючения, если юзер не является владельцем бронирования
        em.persist(item);
        booking.setItem(item);
        em.persist(booking);

        try {
            bookingService.patchBooking(booking.getId(), 100L, true);
        } catch (EntityNotFoundException e) {
            assertEquals("статус бронирования может менять только владелец вещи", e.getMessage());
        }

        // Сценарий изменения статуса на APPROVED
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);

        BookingReturnDto result = bookingService.patchBooking(booking.getId(), user.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());

        // Сценарий изменения статуса на REJECTED
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);

        result = bookingService.patchBooking(booking.getId(), user.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void getBooking() {
        // Проверка сценария, если бронирование не найдено
        try {
            bookingService.getBooking(100L, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Бронирование не найдено", e.getMessage());
        }

        // Проверка сценария, если пользователь не является автором бронирования
        User user2 = new User();
        user2 = new User();
        user2.setName("TestUser2");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);
        em.persist(item);
        booking.setItem(item);
        em.persist(booking);

        try {
            bookingService.getBooking(booking.getId(), user2.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не является владельцем вещи или автором бронирования", e.getMessage());
        }

        // Сценарий, когда идёт запрос от владельца вещи
        item.setUserId(user2.getId());

        BookingReturnDto result = bookingService.getBooking(booking.getId(), user2.getId());
        assertNotNull(result);

        // Сценарий, когда идёт запрос от автора бронирования
        booking.setBooker(user);

        result = bookingService.getBooking(booking.getId(), user2.getId());
        assertNotNull(result);
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void getUserBookingList() {
        em.persist(item);
        Booking booking2 = new Booking();
        booking2.setItem(item);
        booking2.setBooker(user);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));

        em.persist(booking);
        em.persist(booking2);

        // Сценарий запроса бронирований пользователя с пометкой FUTURE
        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto2);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "FUTURE", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой ALL
        trueResult = new ArrayList<>();
        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult.add(bookingReturnDto2);
        trueResult.add(bookingReturnDto);

        result = bookingService.getUserBookingList(user.getId(), "ALL", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой WAITING
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getUserBookingList(user.getId(), "WAITING", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой REJECTED
        booking.setStatus(BookingStatus.REJECTED);
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getUserBookingList(user.getId(), "REJECTED", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой CURRENT
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getUserBookingList(user.getId(), "CURRENT", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой PAST
        booking.setEnd(LocalDateTime.now().minusHours(4));
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getUserBookingList(user.getId(), "PAST", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса с неизвестной пометкой
        try {
            bookingService.getUserBookingList(user.getId(), "SPECIFIC", 0, 10);
        } catch (EntityNotAvailableException e) {
            assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
        }


    }

    @Test
    void getOwnerBookingList() {
        Item item2 = new Item();
        item2.setAvailable(true);
        item2.setName("TestItem");
        item2.setDescription("Test description");
        item2.setUserId(user.getId());
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        Booking booking2 = new Booking();
        booking2.setItem(item2);
        booking2.setBooker(user2);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);

        em.persist(booking);
        em.persist(booking2);

        // Сценарий запроса бронирований пользователя с пометкой FUTURE
        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto2);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(
                user.getId(), "FUTURE", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой ALL
        trueResult = new ArrayList<>();
        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult.add(bookingReturnDto);
        trueResult.add(bookingReturnDto2);

        result = bookingService.getOwnerBookingList(user.getId(), "ALL", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой WAITING
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getOwnerBookingList(user.getId(), "WAITING", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой REJECTED
        booking.setStatus(BookingStatus.REJECTED);
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getOwnerBookingList(user.getId(), "REJECTED", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой CURRENT
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getOwnerBookingList(user.getId(), "CURRENT", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса бронирований пользователя с пометкой PAST
        booking.setEnd(LocalDateTime.now().minusHours(4));
        em.persist(booking);
        bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult = List.of(bookingReturnDto);

        result = bookingService.getOwnerBookingList(user.getId(), "PAST", 0, 10);

        assertEquals(trueResult, result);

        // Сценарий запроса с неизвестной пометкой
        try {
            bookingService.getUserBookingList(user.getId(), "SPECIFIC", 0, 10);
        } catch (EntityNotAvailableException e) {
            assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
        }
    }
}