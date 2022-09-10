package ru.practicum.shareit.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.NotFoundUserException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(UserRepository userRepository,
                                  ItemRequestRepository itemRequestRepository,
                                  ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequest createRequest(long userId, ItemRequest request) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            request.setRequestor(user.get());
            request.setCreated(LocalDateTime.now());
            log.info("Добавлен новый запрос пользователем id = {}", userId);
            return itemRequestRepository.save(request);
        } else {
            throw new NotFoundUserException(String.format("Не найден пользователь id = %s", userId));
        }
    }

    @Override
    public List<ItemRequestDto> getRequestsByOwner(long userId) {
        if (userRepository.findById(userId).isPresent()) {
            log.info("Запрошены все запросы пользователя id = {}", userId);
            return itemRequestRepository.findAllByRequestorId(userId).stream()
                    .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                    .map(ItemRequestMapper::toItemRequestDto)
                    .peek(s -> s.setItems(itemRepository.findItemsByRequest(s.getId()).stream()
                            .map(ItemMapper::toItemDto)
                            .collect(Collectors.toSet())))
                    .collect(Collectors.toList());
        } else {
            throw new NotFoundUserException(String.format("Не найден пользователь id = %s", userId));
        }
    }

    @Override
    public List<ItemRequestDto> getAllRequest(long userId, int from, int size) {
        if (userRepository.findById(userId).isPresent()) {
            int page = from / size;
            Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
            log.info("Пользователь id = {} запросил все запросы", userId);
            return itemRequestRepository.findAll(pageable).get()
                    .filter(s -> s.getRequestor().getId() != userId)
                    .map(ItemRequestMapper::toItemRequestDto)
                    .peek(s -> s.setItems(itemRepository.findItemsByRequest(s.getId()).stream()
                            .map(ItemMapper::toItemDto)
                            .collect(Collectors.toSet())))
                    .collect(Collectors.toList());
        } else {
            throw new NotFoundUserException(String.format("Не найден пользователь id = %s", userId));
        }
    }

    @Override
    public ItemRequestDto getRequestById(long userId, long requestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundUserException(String.format("Не найден пользователь id = %s", userId)));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.findById(requestId)
                        .orElseThrow(() -> new NotFoundRequestException(
                                String.format("Не найден запрос id = %s", requestId)))
        );
        itemRequestDto.setItems(itemRepository.findItemsByRequest(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toSet()));
        log.info("Пользователь id = {} запросил запросы id = {}", userId, requestId);
        return itemRequestDto;
    }
}