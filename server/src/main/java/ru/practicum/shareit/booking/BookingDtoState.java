package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.State;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoState {
    private long userId;
    private State state;
}