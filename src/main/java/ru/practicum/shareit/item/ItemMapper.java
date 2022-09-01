package ru.practicum.shareit.item;

public class ItemMapper {
    public static Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static ItemDtoWithBooking toItemDtoWithBooking(Item item) {
        ItemDtoWithBooking itemDtoWithBooking = new ItemDtoWithBooking(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
        if (item.getRequest() != null) {
            itemDtoWithBooking.setRequestId(item.getRequest().getId());
        }
        return itemDtoWithBooking;
    }
}