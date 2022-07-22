package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (userStorage.getUser(userId) != null) {
            return itemStorage.addItem(userId, itemDto);
        } else {
            return null;
        }
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, long itemId) {
        if (userStorage.getUser(userId) != null) {
            return itemStorage.updateItem(userId, itemDto, itemId);
        } else {
            return null;
        }
    }

    @Override
    public ItemDto getItem(long itemId) {
        return itemStorage.getItem(itemId);
    }

    @Override
    public Collection<ItemDto> getAllItem(Long userId) {
        return itemStorage.getAllItem(userId);
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (!text.isEmpty()) {
            return itemStorage.searchItems(text);
        } else {
            return new ArrayList<>();
        }
    }
}