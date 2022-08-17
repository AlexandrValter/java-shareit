package ru.practicum.shareit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.booking.NotAvailableBookingException;
import ru.practicum.shareit.booking.NotFoundBookingException;
import ru.practicum.shareit.item.NotAvailableItemException;
import ru.practicum.shareit.item.NotFoundItemException;
import ru.practicum.shareit.user.DuplicateEmailException;
import ru.practicum.shareit.user.NotFoundUserException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Error> catchDuplicateEmail(DuplicateEmailException e) {
        return new ResponseEntity<>(new Error(HttpStatus.CONFLICT.value(), e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<Error> catchNotFoundUser(NotFoundUserException e) {
        return new ResponseEntity<>(new Error(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Error> catchNotFoundItemException(NotFoundItemException e) {
        return new ResponseEntity<>(new Error(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Error> catchNotAvailableItemException(NotAvailableItemException e) {
        return new ResponseEntity<>(new Error(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Error> catchNotAvailableBookingException(NotAvailableBookingException e) {
        return new ResponseEntity<>(new Error(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> catchMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", String.format("Unknown %s: %s", e.getName(), e.getValue()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Error> catchNotFoundBookingException(NotFoundBookingException e) {
        return new ResponseEntity<>(new Error(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }
}