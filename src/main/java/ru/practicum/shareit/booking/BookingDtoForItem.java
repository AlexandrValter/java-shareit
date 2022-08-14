package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingDtoForItem {
    private long id;
    private long bookerId;
}
