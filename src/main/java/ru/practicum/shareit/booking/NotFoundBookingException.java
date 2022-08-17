package ru.practicum.shareit.booking;

public class NotFoundBookingException extends RuntimeException {

    public NotFoundBookingException(String message) {
        super(message);
    }
}
