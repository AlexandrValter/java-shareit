package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    private Booking createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    private Booking getBooking(@PathVariable long bookingId,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @PatchMapping("/{bookingId}")
    private Booking changeStatus(@PathVariable long bookingId,
                                 @RequestParam boolean approved,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.changeStatus(bookingId, approved, userId);
    }

    @GetMapping
    private List<Booking> getAllBooking(@RequestParam(defaultValue = "ALL") State state,
                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getAllBooking(new BookingDtoState(userId, state));
    }

    @GetMapping("/owner")
    private List<Booking> getAllBookingByOwner(@RequestParam(defaultValue = "ALL") State state,
                                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getAllBookingByOwner(new BookingDtoState(userId, state));
    }
}