package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    private ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.createBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    private ResponseEntity<Object> getBooking(@PathVariable long bookingId,
                                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingClient.getBooking(bookingId, userId);
    }

    @PatchMapping("/{bookingId}")
    private ResponseEntity<Object> changeStatus(@PathVariable long bookingId,
                                                @RequestParam boolean approved,
                                                @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingClient.changeStatus(bookingId, approved, userId);
    }

    @GetMapping
    private ResponseEntity<Object> getAllBooking(@RequestParam(defaultValue = "ALL") String requestState,
                                                 @RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        State state = State.from(requestState)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + requestState));
        return bookingClient.getAllBooking(userId, state, from, size);
    }
}
