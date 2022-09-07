package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;
    private final User user1 = new User();
    private final User user2 = new User();
    private final ItemRequest request = new ItemRequest();
    private final ItemDto itemDto = new ItemDto();
    private final BookingDto lastBookingDto = new BookingDto(
            1L,
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now().minusDays(2)
    );
    private final BookingDto nextBookingDto = new BookingDto(
            1L,
            LocalDateTime.now().plusDays(3),
            LocalDateTime.now().plusDays(5)
    );
    private final CommentDtoFromRequest commentDto = new CommentDtoFromRequest();

    @BeforeEach
    public void restartIdentity() {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE;").executeUpdate();
        em.createNativeQuery("TRUNCATE table items restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table users restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table booking restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table requests restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table comments restart identity;").executeUpdate();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE;").executeUpdate();
        user1.setName("User1");
        user1.setEmail("user1@test.ru");
        user2.setName("User2");
        user2.setEmail("user2@test.ru");
        user2.setEmail("user2@test.ru");
        userService.createUser(user1);
        userService.createUser(user2);
        request.setCreated(LocalDateTime.now());
        request.setRequestor(user2);
        request.setDescription("Request item");
        itemRequestService.createRequest(user2.getId(), request);
        itemDto.setRequestId(1L);
        itemDto.setAvailable(true);
        itemDto.setDescription("Description item");
        itemDto.setName("New item");
    }

    @Test
    public void test1_createItem() {
        Item item = itemService.createItem(itemDto, user1.getId());
        assertThat(item.getId(), equalTo(1L));
        assertThat(item.getRequest(), equalTo(request));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getOwner(), equalTo(user1));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getAvailable(), equalTo(true));
    }

    @Test
    public void test2_updateItem() {
        Item item = itemService.createItem(itemDto, user1.getId());
        ItemDto updateItem = new ItemDto();
        updateItem.setName("Update name");
        updateItem.setDescription("Description after update");
        updateItem.setAvailable(false);
        updateItem.setRequestId(1L);
        Item itemAfterUpdate = itemService.updateItem(user1.getId(), updateItem, item.getId());
        assertThat(itemAfterUpdate.getId(), equalTo(1L));
        assertThat(itemAfterUpdate.getRequest(), equalTo(request));
        assertThat(itemAfterUpdate.getDescription(), equalTo(updateItem.getDescription()));
        assertThat(itemAfterUpdate.getOwner(), equalTo(user1));
        assertThat(itemAfterUpdate.getName(), equalTo(updateItem.getName()));
        assertThat(itemAfterUpdate.getAvailable(), equalTo(false));
    }

    @Test
    public void test3_getItem() {
        Item item = itemService.createItem(itemDto, user1.getId());
        Booking lastBooking = bookingService.createBooking(user2.getId(), lastBookingDto);
        Booking nextBooking = bookingService.createBooking(user2.getId(), nextBookingDto);
        commentDto.setText("Comment for item1");
        Comment comment = itemService.createComment(user2.getId(), item.getId(), commentDto);
        ItemDtoWithBooking itemDtoWithBooking = itemService.getItem(item.getId(), user1.getId());
        assertThat(itemDtoWithBooking.getId(), equalTo(1L));
        assertThat(itemDtoWithBooking.getName(), equalTo(item.getName()));
        assertThat(itemDtoWithBooking.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDtoWithBooking.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDtoWithBooking.getRequestId(), equalTo(request.getId()));
        assertThat(itemDtoWithBooking.getLastBooking(), equalTo(BookingMapper.toBookingDtoForItem(lastBooking)));
        assertThat(itemDtoWithBooking.getNextBooking(), equalTo(BookingMapper.toBookingDtoForItem(nextBooking)));
        assertThat(itemDtoWithBooking.getComments(), equalTo(Set.of(CommentMapper.toCommentDto(comment))));
    }

    @Test
    public void test4_getAllItem() {
        Item item1 = itemService.createItem(itemDto, user1.getId());
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setAvailable(false);
        itemDto2.setDescription("Description item 2");
        itemDto2.setName("Just item");
        Booking lastBooking = bookingService.createBooking(user2.getId(), lastBookingDto);
        Item item2 = itemService.createItem(itemDto2, user1.getId());
        commentDto.setText("Comment for item1");
        Comment comment = itemService.createComment(user2.getId(), item1.getId(), commentDto);
        List<ItemDtoWithBooking> itemDtoWithBookingList = List.copyOf(itemService.getAllItem(user1.getId(), 0, 5));
        assertThat(itemDtoWithBookingList.size(), equalTo(2));
        assertThat(itemDtoWithBookingList.get(0).getName(), equalTo(item1.getName()));
        assertThat(itemDtoWithBookingList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemDtoWithBookingList.get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(itemDtoWithBookingList.get(0).getRequestId(), equalTo(request.getId()));
        assertThat(itemDtoWithBookingList.get(0).getLastBooking(), equalTo(BookingMapper.toBookingDtoForItem(lastBooking)));
        assertThat(itemDtoWithBookingList.get(0).getNextBooking(), nullValue());
        assertThat(itemDtoWithBookingList.get(0).getComments(), equalTo(Set.of(CommentMapper.toCommentDto(comment))));
        assertThat(itemDtoWithBookingList.get(1).getName(), equalTo(item2.getName()));
        assertThat(itemDtoWithBookingList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(itemDtoWithBookingList.get(1).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(itemDtoWithBookingList.get(1).getRequestId(), equalTo(0L));
        assertThat(itemDtoWithBookingList.get(1).getLastBooking(), nullValue());
        assertThat(itemDtoWithBookingList.get(1).getNextBooking(), nullValue());
        assertThat(itemDtoWithBookingList.get(1).getComments(), equalTo(new HashSet<>()));
    }

    @Test
    public void test5_searchItems() {
        Item item = itemService.createItem(itemDto, user1.getId());
        String text = "new item";
        List<Item> itemList = List.copyOf(itemService.searchItems(text, 0, 5));
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0), equalTo(item));
    }

    @Test
    public void test6_createComment() {
        Item item1 = itemService.createItem(itemDto, user1.getId());
        Booking lastBooking = bookingService.createBooking(user2.getId(), lastBookingDto);
        commentDto.setText("Comment for item1");
        Comment comment = itemService.createComment(user2.getId(), item1.getId(), commentDto);
        assertThat(comment.getId(), equalTo(1L));
        assertThat(comment.getItem(), equalTo(item1));
        assertThat(comment.getCreated(), notNullValue());
        assertThat(comment.getAuthor(), equalTo(user2));
        assertThat(comment.getText(), equalTo(commentDto.getText()));
    }
}