package ru.practicum.shareit.booking.Service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final BookingService bookingService;
    private User user;
    private Item item;
    private Item item2;
    private Booking booking;
    private Booking booking2;
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

        item2 = new Item();
        item2.setAvailable(true);
        item2.setName("TestItem");
        item2.setDescription("Test description");
        item2.setUserId(user.getId());

        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(21));
        booking.setEnd(LocalDateTime.now().minusDays(10));

        booking2 = new Booking();
        booking2.setItem(item);
        booking2.setBooker(user);
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table bookings");
    }


    @Test
    void addBookingWhenItemNotAvailable() {
        // ???????????????? ????????????, ?????????? ?????????????? ????????????????????
        item.setAvailable(false);
        em.persist(item);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(21));
        bookingDto.setEnd(LocalDateTime.now().minusDays(10));

        EntityNotAvailableException thrown = Assertions
                .assertThrows(EntityNotAvailableException.class, () ->
                        bookingService.addBooking(bookingDto, user.getId()));

        assertEquals("?????????????? ????????????????????", thrown.getMessage());
    }

    @Test
    void addBookingWhenUserWasItemOwner() {
        // ???????????????? ?????????????? ???????????????????????? ???????????? ????????????????
        item.setAvailable(true);
        item.setUserId(user.getId());
        em.persist(item);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(21));
        bookingDto.setEnd(LocalDateTime.now().minusDays(10));

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        bookingService.addBooking(bookingDto, user.getId()));

        assertEquals("???????????????????? ?????????????????????????? ???????? ??????????????", thrown.getMessage());
    }

    @Test
    void addBookingWhenCorrectValues() {
        // ???????????????? ?????????????????????? ????????????
        item.setAvailable(true);
        em.persist(item);
        item.setUserId(100L);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(21));
        bookingDto.setEnd(LocalDateTime.now().minusDays(10));
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
    }

    @Test
    void patchBookingWhenBookingNotFound() {
        // ???????????????? ????????????????????, ???????? ???????????????????????? ???? ??????????????
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        bookingService.patchBooking(100L, user.getId(), true));

        assertEquals("???????????????????????? ???? ??????????????", thrown.getMessage());
    }

    @Test
    void patchBookingWhenUserIsNotOwner() {
        // ???????????????? ??????????????????, ???????? ???????? ???? ???????????????? ???????????????????? ????????????????????????
        em.persist(item);
        booking.setItem(item);
        em.persist(booking);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        bookingService.patchBooking(booking.getId(), 100L, true));

        assertEquals("???????????? ???????????????????????? ?????????? ???????????? ???????????? ???????????????? ????????", thrown.getMessage());
    }

    @Test
    void patchBookingWhenStatusIsApproved() {
        // ???????????????? ?????????????????? ?????????????? ???? APPROVED
        em.persist(item);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);

        BookingReturnDto result = bookingService.patchBooking(booking.getId(), user.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void patchBookingWhenStatusIsRejected() {
        // ???????????????? ?????????????????? ?????????????? ???? REJECTED
        em.persist(item);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);

        BookingReturnDto result = bookingService.patchBooking(booking.getId(), user.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void getBookingWhenBookingNotFound() {
        // ???????????????? ????????????????, ???????? ???????????????????????? ???? ??????????????
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        bookingService.getBooking(100L, user.getId()));

        assertEquals("???????????????????????? ???? ??????????????", thrown.getMessage());
    }

    @Test
    void getBookingWhenUserIsNotOwner() {
        // ???????????????? ????????????????, ???????? ???????????????????????? ???? ???????????????? ?????????????? ????????????????????????
        User user2 = new User();
        user2.setName("TestUser2");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);
        em.persist(item);
        booking.setItem(item);
        em.persist(booking);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        bookingService.getBooking(booking.getId(), user2.getId()));

        assertEquals("???????????????????????? ???? ???????????????? ???????????????????? ???????? ?????? ?????????????? ????????????????????????", thrown.getMessage());
    }

    @Test
    void getBookingWhenRequestFromItemOwner() {
        // ????????????????, ?????????? ???????? ???????????? ???? ?????????????????? ????????
        User user2 = new User();
        user2.setName("TestUser2");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);
        item.setUserId(user2.getId());
        em.persist(item);
        booking.setItem(item);
        em.persist(booking);

        BookingReturnDto result = bookingService.getBooking(booking.getId(), user2.getId());
        assertNotNull(result);
    }

    @Test
    void getBookingWhenRequestFromBookingOwner() {
        // ????????????????, ?????????? ???????? ???????????? ???? ???????????? ????????????????????????
        User user2 = new User();
        user2.setName("TestUser2");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);
        item.setUserId(user2.getId());
        em.persist(item);
        booking.setItem(item);
        booking.setBooker(user);
        em.persist(booking);

        BookingReturnDto result = bookingService.getBooking(booking.getId(), user2.getId());

        assertNotNull(result);
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void getUserBookingListWhenBookingIsFuture() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? FUTURE
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto2);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "FUTURE", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenBookingIsAll() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? ALL
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        em.persist(booking);
        em.persist(booking2);
        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);

        List<BookingReturnDto> trueResult = new ArrayList<>();
        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        trueResult.add(bookingReturnDto2);
        trueResult.add(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "ALL", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenBookingIsWaiting() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? WAITING
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "WAITING", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenBookingIsRejected() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? REJECTED
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setStatus(BookingStatus.REJECTED);
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "REJECTED", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenBookingIsCurrent() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? CURRENT
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "CURRENT", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenBookingIsPast() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? PAST
        em.persist(item);
        booking2.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().minusHours(4));
        booking.setStatus(BookingStatus.APPROVED);
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getUserBookingList(user.getId(), "PAST", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getUserBookingListWhenUnsupportedStatus() {
        // ???????????????? ?????????????? ?? ?????????????????????? ????????????????
        EntityNotAvailableException thrown = Assertions
                .assertThrows(EntityNotAvailableException.class, () ->
                        bookingService.getUserBookingList(user.getId(), "SPECIFIC", 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", thrown.getMessage());
    }

    @Test
    void getOwnerBookingListWhenBookingIsFuture() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? FUTURE
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);

        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto2);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(
                user.getId(), "FUTURE", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenBookingIsAll() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? ALL
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);

        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto2 = BookingMapper.toBookingReturnDto(booking2);
        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);

        List<BookingReturnDto> trueResult = new ArrayList<>();
        trueResult.add(bookingReturnDto2);
        trueResult.add(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(user.getId(), "ALL", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenBookingIsWaiting() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? WAITING
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);
        booking.setStatus(BookingStatus.WAITING);

        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(user.getId(), "WAITING", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenBookingIsRejected() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? REJECTED
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);
        booking.setStatus(BookingStatus.REJECTED);

        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(user.getId(), "REJECTED", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenBookingIsCurrent() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? CURRENT
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(user.getId(), "CURRENT", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenBookingIsPast() {
        // ???????????????? ?????????????? ???????????????????????? ???????????????????????? ?? ???????????????? PAST
        em.persist(item);
        em.persist(item2);

        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);

        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setStart(LocalDateTime.now().plusDays(10));
        booking2.setEnd(LocalDateTime.now().plusDays(15));
        booking.setBooker(user2);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().minusHours(4));
        em.persist(booking);
        em.persist(booking2);

        BookingReturnDto bookingReturnDto = BookingMapper.toBookingReturnDto(booking);
        List<BookingReturnDto> trueResult = List.of(bookingReturnDto);

        List<BookingReturnDto> result = bookingService.getOwnerBookingList(user.getId(), "PAST", 0, 10);

        assertEquals(trueResult, result);
    }

    @Test
    void getOwnerBookingListWhenUnsupportedStatus() {
        // ???????????????? ?????????????? ?? ?????????????????????? ????????????????
        EntityNotAvailableException thrown = Assertions
                .assertThrows(EntityNotAvailableException.class, () ->
                        bookingService.getUserBookingList(user.getId(), "SPECIFIC", 0, 10));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", thrown.getMessage());
    }
}