package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingDtoState {
    private long userId;
    private State state;
}
