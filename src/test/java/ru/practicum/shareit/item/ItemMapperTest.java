package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

public class ItemMapperTest {

    @Test
    public void test1_tryConvertToItemDtoWhenRequestIsNotNull() {
        Item item = new Item(1L, "Test", "Item for test", true);
        ItemRequest request = new ItemRequest();
        request.setDescription("Description");
        request.setRequestor(new User(5L, "User", "test@test.test"));
        request.setId(7L);
        item.setRequest(request);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Assertions.assertEquals(item.getId(), itemDto.getId());
        Assertions.assertEquals(item.getAvailable(), itemDto.getAvailable());
        Assertions.assertEquals(item.getName(), itemDto.getName());
        Assertions.assertEquals(item.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(item.getRequest().getId(), itemDto.getRequestId());
    }

    @Test
    public void test2_tryConvertToItemDtoWhenRequestIsNull() {
        Item item = new Item(1L, "Test", "Item for test", true);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Assertions.assertEquals(item.getId(), itemDto.getId());
        Assertions.assertEquals(item.getAvailable(), itemDto.getAvailable());
        Assertions.assertEquals(item.getName(), itemDto.getName());
        Assertions.assertEquals(item.getDescription(), itemDto.getDescription());
        Assertions.assertNull(itemDto.getRequestId());
    }

    @Test
    public void test3_tryConvertToItemDtoWithBookingWhenRequestIsNotNull() {
        Item item = new Item(1L, "Test", "Item for test", true);
        ItemRequest request = new ItemRequest();
        request.setDescription("Description");
        request.setRequestor(new User(5L, "User", "test@test.test"));
        request.setId(7L);
        item.setRequest(request);
        ItemDtoWithBooking itemDto = ItemMapper.toItemDtoWithBooking(item);
        Assertions.assertEquals(item.getId(), itemDto.getId());
        Assertions.assertEquals(item.getAvailable(), itemDto.getAvailable());
        Assertions.assertEquals(item.getName(), itemDto.getName());
        Assertions.assertEquals(item.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(item.getRequest().getId(), itemDto.getRequestId());
    }

    @Test
    public void test4_tryConvertToItemDtoWithBookingWhenRequestIsNull() {
        Item item = new Item(1L, "Test", "Item for test", true);
        ItemDtoWithBooking itemDto = ItemMapper.toItemDtoWithBooking(item);
        Assertions.assertEquals(item.getId(), itemDto.getId());
        Assertions.assertEquals(item.getAvailable(), itemDto.getAvailable());
        Assertions.assertEquals(item.getName(), itemDto.getName());
        Assertions.assertEquals(item.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(0, itemDto.getRequestId());
    }
}