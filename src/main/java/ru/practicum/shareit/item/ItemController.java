package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable long itemId) {
        return itemService.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItem(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }
}