package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.NotFoundUserException;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, List<Item>> items = new HashMap<>();
    private Long id;

    @Override
    public ItemDto addItem(Long userId, Item item) {
        item.setId(makeId());
        if (items.containsKey(userId)) {
            items.get(userId).add(item);
        } else {
            items.put(userId, new ArrayList<>());
            items.get(userId).add(item);
        }
        log.info("Пользователь id={} добавил новую вещь id={}", userId, item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, long itemId) {
        if (items.get(userId) != null) {
            if (items.get(userId).contains(getItem(itemId))) {
                Item newItem = getItem(itemId);
                if (itemDto.getId() != null) {
                    newItem.setId(itemDto.getId());
                }
                if (itemDto.getName() != null) {
                    newItem.setName(itemDto.getName());
                }
                if (itemDto.getDescription() != null) {
                    newItem.setDescription(itemDto.getDescription());
                }
                if (itemDto.getAvailable() != null) {
                    newItem.setAvailable(itemDto.getAvailable());
                }
                deleteItem(userId, itemId);
                items.get(userId).add(newItem);
                log.info("Пользователь id={} изменил информацию о своей вещи id={}", userId, itemId);
                return ItemMapper.toItemDto(newItem);
            } else {
                throw new NotFoundItemException(String.format("У пользователя с id = %s нет вещи с id = %s",
                        userId, itemId));
            }
        } else {
            throw new NotFoundUserException(String.format("Пользователь с id = %s еще не добавлял вещей", userId));
        }
    }

    @Override
    public Item getItem(long itemId) {
        Item item = null;
        while (item == null) {
            for (List<Item> itemList : items.values()) {
                item = itemList.stream()
                        .filter(i -> i.getId() == itemId)
                        .findFirst()
                        .orElse(null);
            }
        }
        log.info("Запрошена вещь id={}", itemId);
        return item;
    }

    @Override
    public Collection<ItemDto> getAllItem(Long userId) {
        log.info("Запрошены все вещи пользователя id={}", userId);
        return items.get(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        List<ItemDto> findItems = new ArrayList<>();
        for (List<Item> itemList : items.values()) {
            for (Item item : itemList) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable()) {
                    findItems.add(ItemMapper.toItemDto(item));
                }
            }
        }
        log.info("Запрошен поиск в базе по тексту: {}", text);
        return findItems;
    }

    private void deleteItem(long userId, long itemId) {
        items.get(userId).removeIf(item -> item.getId() == itemId);
    }

    private Long makeId() {
        return (id == null) ? id = 1L : ++id;
    }
}