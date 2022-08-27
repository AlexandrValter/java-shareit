package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.NotAvailableBookingException;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl service;
    private final Item item1 = new Item(1L, "Item1", "Description for item 1", true);
    private final Item item2 = new Item(2L, "Item2", "Description for item 2", true);
    private final User user1 = new User(1L, "User1", "user1@ya.ru");
    private final User user2 = new User(2L, "User2", "user2@ya.ru");
    private final ItemRequest request1 = new ItemRequest(1L, "Request", user2, LocalDateTime.now());
    private final Comment comment1 = new Comment();
    private int from = 0;
    private int size = 5;

    @Test
    public void test1_tryCreateNewItem() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(item1);
        Item item = service.createItem(ItemMapper.toItemDto(item1), user1.getId());
        Assertions.assertEquals(item1.getId(), item.getId());
    }

    @Test
    public void test2_tryCreateItemWhenUserIsNotFound() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenThrow(NoSuchElementException.class);
        Assertions.assertThrows(NoSuchElementException.class, () ->
                service.createItem(ItemMapper.toItemDto(item1), user1.getId()));
    }

    @Test
    public void test3_tryCreateNewItemWhenRequestIsNotNull() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(item1);
        Mockito
                .when(itemRequestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(request1));
        item1.setRequest(request1);
        Item item = service.createItem(ItemMapper.toItemDto(item1), user1.getId());
        Assertions.assertEquals(item1.getId(), item.getId());
        Assertions.assertEquals(request1, item.getRequest());
    }

    @Test
    public void test4_tryUpdateItem() {
        item1.setRequest(request1);
        item1.setOwner(user1);
        ItemDto newItem = new ItemDto();
        newItem.setName("New_name");
        newItem.setDescription("New description after update");
        newItem.setAvailable(false);
        Item itemAfterUpdate = item1;
        itemAfterUpdate.setName("New_name");
        itemAfterUpdate.setAvailable(false);
        itemAfterUpdate.setDescription("New description after update");
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(itemAfterUpdate);
        Item item = service.updateItem(1L, newItem, 1L);
        Assertions.assertEquals("New_name", item.getName());
        Assertions.assertEquals("New description after update", item.getDescription());
        Assertions.assertEquals(false, item.getAvailable());
        Assertions.assertEquals(1L, item.getId());
        Assertions.assertEquals(user1, item.getOwner());
        Assertions.assertEquals(request1, item.getRequest());
    }

    @Test
    public void test5_tryUpdateItemWhenItemNotFoundInDB() {
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenThrow(NoSuchElementException.class);
        Assertions.assertThrows(NoSuchElementException.class, () ->
                service.updateItem(user1.getId(), ItemMapper.toItemDto(item1), 1L));
    }

    @Test
    public void test6_tryUpdateItemWhenUserIsNotOwnerForItem() {
        item1.setOwner(user1);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user2));
        NotFoundItemException thrown = Assertions.assertThrows(NotFoundItemException.class, () -> {
            service.updateItem(user1.getId(), ItemMapper.toItemDto(item1), 1L);
        });
        Assertions.assertEquals("У пользователя с id = 1 нет вещи с id = 1", thrown.getMessage());
    }

    @Test
    public void test7_tryUpdateItemWhenUserIsNotFoundInDB() {
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenThrow(NoSuchElementException.class);
        Assertions.assertThrows(NoSuchElementException.class, () ->
                service.updateItem(user1.getId(), ItemMapper.toItemDto(item1), 1L));
    }

    @Test
    public void test8_tryGetItem() {
        item1.setOwner(user1);
        item1.setRequest(request1);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        comment1.setAuthor(user2);
        comment1.setItem(item1);
        comment1.setText("Comment");
        comment1.setId(1L);
        Mockito
                .when(commentRepository.findCommentsByItem_Id(item1.getId()))
                .thenReturn(Set.of(comment1));
        ItemDtoWithBooking item = service.getItem(1L, 1L);
        Assertions.assertEquals("Item1", item.getName());
        Assertions.assertEquals(1L, item.getId());
        Assertions.assertEquals("Description for item 1", item.getDescription());
        Assertions.assertEquals(true, item.getAvailable());
        Assertions.assertEquals(Set.of(CommentMapper.toCommentDto(comment1)), item.getComments());
        Assertions.assertNull(item.getLastBooking());
    }

    @Test
    public void test9_tryGetItemWhenItemIsNotFoundInDB() {
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundItemException thrown = Assertions.assertThrows(NotFoundItemException.class, () ->
                service.getItem(item1.getId(), user1.getId()));
        Assertions.assertEquals("Вещь с id = 1 не найдена", thrown.getMessage());
    }

    @Test
    public void test10_tryGetAllItem() {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        item1.setOwner(user1);
        item2.setOwner(user1);
        List<Item> list = List.of(item1, item2);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Item> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        comment1.setAuthor(user2);
        comment1.setItem(item1);
        comment1.setText("Comment");
        comment1.setId(1L);
        Mockito
                .when(itemRepository.findAllByOwnerId(user1.getId(), pageable))
                .thenReturn(page);
        Mockito
                .when(commentRepository.findCommentsByItem_Id(item1.getId()))
                .thenReturn(Set.of(comment1));
        Collection<ItemDtoWithBooking> itemsDto = service.getAllItem(user1.getId(), from, size);
        List<ItemDtoWithBooking> items = Collections.unmodifiableList(new ArrayList<>(itemsDto));
        ItemDtoWithBooking itemDto1 = ItemMapper.toItemDtoWithBooking(item1);
        itemDto1.setComments(Set.of(CommentMapper.toCommentDto(comment1)));
        Assertions.assertEquals(2, itemsDto.size());
        Assertions.assertEquals(Set.of(CommentMapper.toCommentDto(comment1)), items.get(0).getComments());
        Assertions.assertEquals(itemDto1, items.get(0));
        Assertions.assertEquals(ItemMapper.toItemDtoWithBooking(item2), items.get(1));
    }

    @Test
    public void test11_tryGetAllItemWhenSizeOrFromIsIncorrect() {
        ArithmeticException thrown1 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllItem(user1.getId(), 0, 0));
        ArithmeticException thrown2 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllItem(user1.getId(), -1, 1));
        ArithmeticException thrown3 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllItem(user1.getId(), -1, 0));
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown1.getMessage()
        );
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown2.getMessage()
        );
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown3.getMessage()
        );
    }

    @Test
    public void test12_trySearchItems() {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> list = List.of(item1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Item> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(itemRepository.findItems("Item1", pageable))
                .thenReturn(page);
        List<Item> itemList = List.copyOf(service.searchItems("Item1", from, size));
        Assertions.assertEquals(1, itemList.size());
    }

    @Test
    public void test13_trySearchItemsWhenTextIsEmpty() {
        String text = "";
        List<Item> emptyList = List.copyOf(service.searchItems(text, from, size));
        Assertions.assertTrue(emptyList.isEmpty());
    }

    @Test
    public void test14_trySearchItemsWhenSizeOrFromIsIncorrect() {
        ArithmeticException thrown1 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.searchItems("Item1", 0, 0));
        ArithmeticException thrown2 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.searchItems("Item1", -1, 1));
        ArithmeticException thrown3 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.searchItems("Item1", -1, 0));
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown1.getMessage()
        );
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown2.getMessage()
        );
        Assertions.assertEquals(
                "Неверное значение индекса первого элемента или количества элементов для отображения",
                thrown3.getMessage()
        );
    }

    @Test
    public void test15_tryCreateComment() {
        Booking booking = new Booking();
        booking.setItem(item1);
        booking.setId(1L);
        booking.setStatus(Status.APPROVED);
        booking.setBooker(user2);
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        CommentDtoFromRequest commentDtoFromRequest = new CommentDtoFromRequest();
        commentDtoFromRequest.setText("Text new comment");
        Mockito
                .when(bookingRepository.findByBooker_IdAndEndIsBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito.when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        Comment newComment = comment1;
        newComment.setText("Text new comment");
        Mockito
                .when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(newComment);
        Comment comment = service.createComment(user2.getId(), item1.getId(), commentDtoFromRequest);
        Assertions.assertEquals(newComment, comment);
    }

    @Test
    public void test16_tryCreateCommentWhenUserIsNotBooker() {
        Booking booking = new Booking();
        booking.setItem(new Item(3L, "Item3", "Item item", true));
        booking.setId(1L);
        booking.setStatus(Status.APPROVED);
        booking.setBooker(user2);
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        CommentDtoFromRequest commentDtoFromRequest = new CommentDtoFromRequest();
        commentDtoFromRequest.setText("Text new comment");
        Mockito
                .when(bookingRepository.findByBooker_IdAndEndIsBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        NotAvailableBookingException thrown = Assertions.assertThrows(NotAvailableBookingException.class, () ->
                service.createComment(user2.getId(), item1.getId(), commentDtoFromRequest));
        Assertions.assertEquals(
                "Пользователь id = 2 не брал в аренду вещь id = 1, или аренда еще не завершена",
                thrown.getMessage()
        );
    }

    @Test
    public void test17_tryUpdateItemWhenFieldsIsNull() {
        item1.setRequest(request1);
        item1.setOwner(user1);
        ItemDto newItem = new ItemDto();
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(item1);
        Item item = service.updateItem(1L, newItem, 1L);
        Assertions.assertEquals(item1, item);
    }
}