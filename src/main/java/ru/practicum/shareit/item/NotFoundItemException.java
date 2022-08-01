package ru.practicum.shareit.item;

public class NotFoundItemException extends RuntimeException {

    public NotFoundItemException(String message) {
        super(message);
    }
}