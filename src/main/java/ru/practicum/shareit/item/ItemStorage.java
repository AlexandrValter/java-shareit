package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemStorage {
    ItemDto addItem(Long userId, Item item);

    ItemDto updateItem(Long userId, ItemDto itemDto, long itemId);

    Item getItem(long itemId);

    Collection<ItemDto> getAllItem(Long userId);

    Collection<ItemDto> searchItems(String text);
}