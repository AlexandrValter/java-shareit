package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime end);

    List<Booking> findByBooker_IdAndStartIsAfter(long bookerId, LocalDateTime start);

    List<Booking> findByBooker_Id(long bookerId);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentBooking(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where b.booker.id = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByBookerAndStatus(long bookerId, Status state);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByOwnerAndStatus(long bookerId, Status state);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByOwnerPast(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByOwnerFuture(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByOwnerCurrent(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findBookingByOwner(long bookerId);

    @Query(nativeQuery = true,
            value = "select * from booking " +
                    "left join items i on booking.item_id = i.id " +
                    "where item_id = ?1 " +
                    "and end_date < ?2 " +
                    "order by end_date desc " +
                    "limit 1")
    Booking findLastItemBooking(long itemId, LocalDateTime now);

    @Query(nativeQuery = true,
            value = "select * from booking " +
                    "left join items i on booking.item_id = i.id " +
                    "where item_id = ?1 " +
                    "and booking.start_date > ?2 " +
                    "order by end_date limit 1")
    Booking findNextItemBooking(long itemId, LocalDateTime now);
}