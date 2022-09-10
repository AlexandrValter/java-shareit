package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.NotFoundUserException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemRequestServiceImpl service;
    private final User user1 = new User(1L, "Test User 1", "test1@email.com");
    private final User user2 = new User(2L, "Test User 1", "test1@email.com");
    private final ItemRequest itemRequest1 = new ItemRequest(
            1L,
            "test request 1",
            user1,
            LocalDateTime.now().minusHours(1L));
    private final ItemRequest itemRequest2 = new ItemRequest(
            2L,
            "test request 2",
            user1,
            LocalDateTime.now().minusHours(2L));
    private final ItemRequest itemRequest3 = new ItemRequest(
            3L,
            "test request 3",
            user2,
            LocalDateTime.now());
    private final Item item1 = new Item(1L, "Test item", "Item for test", true);
    private final Item item2 = new Item(2L, "Test item", "Item for test", true);

    @Test
    public void test1_checkAddNewItemRequest() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRequestRepository.save(Mockito.eq(itemRequest1)))
                .thenReturn(itemRequest1);
        ItemRequest newItem = service.createRequest(1L, itemRequest1);
        Assertions.assertEquals(itemRequest1, newItem);
    }

    @Test
    public void test2_checkAddNewItemRequestWhenUserIdIsIncorrect() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> service.createRequest(1L, itemRequest1));
        Assertions.assertEquals("Не найден пользователь id = 1", ex.getMessage());
    }

    @Test
    public void test3_checkGetAllRequestsByOwner() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRequestRepository.findAllByRequestorId(1L))
                .thenReturn(List.of(itemRequest2, itemRequest1));
        List<ItemRequestDto> list1 = Stream.of(itemRequest1, itemRequest2)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        List<ItemRequestDto> list2 = Stream.of(itemRequest2, itemRequest1)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        Assertions.assertEquals(list1, service.getRequestsByOwner(1L));
        Assertions.assertNotEquals(list2, service.getRequestsByOwner(1L));
    }

    @Test
    public void test4_checkGetAllRequestsByOwnerWhenOwnerIdIsNotCorrect() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> service.getRequestsByOwner(1L));
        Assertions.assertEquals(ex.getMessage(), "Не найден пользователь id = 1");
    }

    @Test
    public void test5_checkGetAllRequestsByOwnerWithItems() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRequestRepository.findAllByRequestorId(1L))
                .thenReturn(List.of(itemRequest2, itemRequest1));
        Mockito
                .when(itemRepository.findItemsByRequest(2L))
                .thenReturn(List.of(item1, item2));
        Mockito
                .when(itemRepository.findItemsByRequest(1L))
                .thenReturn(List.of());
        List<ItemRequestDto> list1 = Stream.of(itemRequest1, itemRequest2)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        list1.get(1).setItems(Stream.of(item1, item2).map(ItemMapper::toItemDto).collect(Collectors.toSet()));
        Assertions.assertEquals(list1, service.getRequestsByOwner(1L));
        Assertions.assertEquals(list1.get(1).getItems(), Stream.of(item1, item2)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toSet()));
    }

    @Test
    public void test6_checkGetAllRequests() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        List<ItemRequest> list = List.of(itemRequest1, itemRequest2, itemRequest3);
        Pageable pageable = PageRequest.of(0, 10);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ItemRequest> page
                = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(itemRequestRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(page);
        List<ItemRequestDto> listDtoUser2 = Stream.of(itemRequest1, itemRequest2)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        Assertions.assertEquals(listDtoUser2, service.getAllRequest(2, 0, 10));
        List<ItemRequestDto> listDtoUser1 = Stream.of(itemRequest3)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        Assertions.assertEquals(listDtoUser1, service.getAllRequest(1, 0, 10));
    }

    @Test
    public void test7_checkGetAllRequestsWhenOwnerIdIsNotCorrect() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> service.getAllRequest(1L, 0, 10));
        Assertions.assertEquals(ex.getMessage(), "Не найден пользователь id = 1");
    }

    @Test
    public void test8_checkGetRequestById() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(itemRequest1));
        item1.setRequest(itemRequest1);
        Mockito
                .when(itemRepository.findItemsByRequest(1L))
                .thenReturn(List.of(item1));
        ItemRequestDto item = ItemRequestMapper.toItemRequestDto(itemRequest1);
        item.setItems(Set.of(ItemMapper.toItemDto(item1)));
        Assertions.assertEquals(item, service.getRequestById(1, 1));
    }

    @Test
    public void test9_checkGetRequestByIdWhenUserIdIsIncorrect() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> service.getRequestById(1L, 1));
        Assertions.assertEquals(ex.getMessage(), "Не найден пользователь id = 1");
    }

    @Test
    public void test10_checkGetRequestByIdWhenRequestIdIsIncorrect() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.empty());
        NotFoundRequestException ex = Assertions.assertThrows(NotFoundRequestException.class,
                () -> service.getRequestById(1L, 1L));
        Assertions.assertEquals(ex.getMessage(), "Не найден запрос id = 1");
    }
}