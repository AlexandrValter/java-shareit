package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto itemDto, long itemId);

    ItemDto getItem(long itemId);

    Collection<ItemDto> getAllItem(Long userId);

    Collection<ItemDto> searchItems(String text);
}