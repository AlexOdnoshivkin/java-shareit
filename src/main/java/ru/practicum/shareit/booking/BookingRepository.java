package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "select b from Booking as b where b.booker.id = ?1 order by b.start desc")
    List<Booking> findBookingsByBooker(Long bookerId);

    @Query(value = "select b from Booking as b where b.booker.id = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findFutureBookingsByBooker(Long bookerId, LocalDateTime dateTime);

    @Query(value = "select b from Booking as b where b.item.id = ?1 order by b.start desc")
    List<Booking> findBookingsByItem(Long itemId);

    @Query(value = "select b from Booking as b where b.item.id = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findFutureBookingsByItem(Long bookerId, LocalDateTime dateTime);

    @Query(value = "select b from Booking as b where b.item.id = ?1 order by b.start asc")
    List<Booking> findBookingsByItemAsc(Long itemId);


    @Query(value = "select b from Booking as b where b.item.id = ?1 and b.status = ?2 order by b.start desc")
    List<Booking> findBookingsByItemAndStatus(Long bookerId, BookingStatus status);

    @Query(value = "select b from Booking as b where b.booker.id = ?1 and b.status = ?2 order by b.start desc")
    List<Booking> findBookingsByBookerAndStatus(Long bookerId, BookingStatus status);

    @Query(value = "select b from Booking as b " +
            "where b.booker.id =?1 and b.item.id = ?2 and b.status <> ?3 order by b.start desc")
    List<Booking> findBookingsByBookerAndItemAndStatusNot(Long userId, Long itemId, BookingStatus status);

    @Query(value = "select b from Booking as b WHERE b.start < ?2 and b.end > ?2 and b.booker.id = ?1 ORDER BY b.start")
    List<Booking> findCurrentBookingForUser(Long userId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE b.start < ?2 and b.end > ?2 and b.item.id = ?1 ORDER BY b.start")
    List<Booking> findCurrentBookingForOwner(Long itemId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE b.end < ?2 and b.booker.id = ?1 ORDER BY b.start")
    List<Booking> findPastBookingForUser(Long userId, LocalDateTime time);

    @Query(value = "select b from Booking as b WHERE  b.end < ?2 and b.item.id = ?1 ORDER BY b.start")
    List<Booking> findPastBookingForOwner(Long itemId, LocalDateTime time);

}
