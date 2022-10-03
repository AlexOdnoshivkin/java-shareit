package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBookingsByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findBookingsByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime dateTime);

    List<Booking> findBookingsByItemIdOrderByStartDesc(Long itemId);

    List<Booking> findBookingsByItemIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime dateTime);

    List<Booking> findBookingsByItemIdOrderByStartAsc(Long itemId);

    List<Booking> findBookingsByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findBookingsByItemIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findBookingByBookerIdAndItemIdAndStatusNot(Long userId, Long itemId, BookingStatus status);

    @Query(value = "select b from Booking as b WHERE b.start < ?2 and b.end > ?2 and b.booker.id = ?1 ORDER BY b.start")
    List<Booking> findCurrentBookingForUser(Long userId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE b.start < ?2 and b.end > ?2 and b.item.id = ?1 ORDER BY b.start")
    List<Booking> findCurrentBookingForOwner(Long itemId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE b.end < ?2 and b.booker.id = ?1 ORDER BY b.start")
    List<Booking> findPastBookingForUser(Long userId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE  b.end < ?2 and b.item.id = ?1 ORDER BY b.start")
    List<Booking> findPastBookingForOwner(Long itemId, LocalDateTime time);

}
