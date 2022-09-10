package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public Booking getBooking(@PathVariable long bookingId,
                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @PatchMapping("/{bookingId}")
    public Booking changeStatus(@PathVariable long bookingId,
                                @RequestParam boolean approved,
                                @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.changeStatus(bookingId, approved, userId);
    }

    @GetMapping
    public List<Booking> getAllBooking(@RequestParam(defaultValue = "ALL") State state,
                                       @RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return bookingService.getAllBooking(new BookingDtoState(userId, state), from, size);
    }

    @GetMapping("/owner")
    public List<Booking> getAllBookingByOwner(@RequestParam(defaultValue = "ALL") State state,
                                              @RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return bookingService.getAllBookingByOwner(new BookingDtoState(userId, state), from, size);
    }
}