package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDto {
    private long itemId;
    @FutureOrPresent
    private LocalDateTime start;
    @FutureOrPresent
    private LocalDateTime end;
}