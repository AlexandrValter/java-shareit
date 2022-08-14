package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(long userId, BookingDto bookingDto);

    Booking getBooking(long bookingId, long userId);

    Booking changeStatus(long bookingId, boolean approved, long userId);

    List<Booking> getAllBooking(BookingDtoState bookingDtoState);

    List<Booking> getAllBookingByOwner(BookingDtoState bookingDtoState);
}