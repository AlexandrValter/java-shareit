package ru.practicum.shareit.requests;

import java.util.List;

public interface ItemRequestService {

    ItemRequest createRequest(long userId, ItemRequest request);

    List<ItemRequestDto> getRequestsByOwner(long userId);

    List<ItemRequestDto> getAllRequest(long userId, int from, int size);

    ItemRequestDto getRequestById(long userId, long requestId);
}