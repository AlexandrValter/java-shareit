package ru.practicum.shareit.item;

public class NotAvailableItemException extends RuntimeException {

    public NotAvailableItemException(String message) {
        super(message);
    }
}
