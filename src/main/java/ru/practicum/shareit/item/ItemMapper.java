package ru.practicum.shareit.item;

public class ItemMapper {
    public static Item toItem (ItemDto itemDto){
        return new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
    }

    public static ItemDto toItemDto (Item item){
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }
}