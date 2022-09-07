package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    Booking createBooking(long userId, BookingDto bookingDto);

    Booking getBooking(long bookingId, long userId);

    Booking changeStatus(long bookingId, boolean approved, long userId);

    List<Booking> getAllBooking(BookingDtoState bookingDtoState, int from, int size);

    List<Booking> getAllBookingByOwner(BookingDtoState bookingDtoState, int from, int size);
}