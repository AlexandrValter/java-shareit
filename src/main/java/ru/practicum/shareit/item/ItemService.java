package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;

import java.util.Collection;

public interface ItemService {
    Item createItem(Item item, long userId);

    Item updateItem(long userId, Item item, long itemId);

    ItemDtoWithBooking getItem(long itemId, long userId);

    Collection<ItemDtoWithBooking> getAllItem(long userId);

    Collection<Item> searchItems(String text);

    Comment createComment(long userId, long itemId, CommentDtoFromRequest comment);
}