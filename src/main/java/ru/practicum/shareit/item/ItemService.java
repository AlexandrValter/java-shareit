package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;

import java.util.Collection;

public interface ItemService {
    Item createItem(ItemDto itemDto, long userId);

    Item updateItem(long userId, ItemDto itemDto, long itemId);

    ItemDtoWithBooking getItem(long itemId, long userId);

    Collection<ItemDtoWithBooking> getAllItem(long userId, int from, int size);

    Collection<Item> searchItems(String text, int from, int size);

    Comment createComment(long userId, long itemId, CommentDtoFromRequest comment);
}