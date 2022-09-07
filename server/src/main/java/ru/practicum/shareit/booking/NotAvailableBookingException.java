package ru.practicum.shareit.booking;

public class NotAvailableBookingException extends RuntimeException {

    public NotAvailableBookingException(String message) {
        super(message);
    }
}