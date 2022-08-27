package ru.practicum.shareit.requests;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestBody @Valid ItemRequest request) {
        return ItemRequestMapper.toItemRequestDto(itemRequestService.createRequest(userId, request));
    }

    @GetMapping
    public List<ItemRequestDto> getRequestsByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getRequestsByOwner(userId);

    }

    @GetMapping(path = "/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllRequest(userId, from, size);
    }

    @GetMapping(path = "/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}