package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:test.properties")
public class BookingServiceTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final User user1 = new User();
    private final User user2 = new User();
    private Item item1 = new Item();
    private BookingDto bookingDto;

    @BeforeEach
    public void restartIdentity() {
        em.createNativeQuery("TRUNCATE table items restart identity CASCADE;").executeUpdate();
        em.createNativeQuery("TRUNCATE table users restart identity CASCADE;").executeUpdate();
        user1.setName("User1");
        user1.setEmail("user1@test.ru");
        userService.createUser(user1);
        user2.setEmail("user2@ya.ru");
        user2.setName("User2");
        userService.createUser(user2);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item1");
        itemDto.setDescription("Item1 for test");
        itemDto.setAvailable(true);
        item1 = itemService.createItem(itemDto, user2.getId());
        bookingDto = new BookingDto(item1.getId(), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    public void test1_createBooking() {
        Booking booking = bookingService.createBooking(user1.getId(), bookingDto);
        assertThat(booking.getId(), equalTo(1L));
        assertThat(booking.getStatus(), equalTo(Status.WAITING));
        assertThat(booking.getItem(), equalTo(item1));
        assertThat(booking.getBooker(), equalTo(user1));
        assertThat(booking.getStart(), notNullValue());
        assertThat(booking.getEnd(), notNullValue());
    }

    @Test
    public void test2_getBooking() {
        bookingService.createBooking(user1.getId(), bookingDto);
        Booking booking = bookingService.getBooking(1L, user1.getId());
        assertThat(booking.getId(), equalTo(1L));
        assertThat(booking.getStatus(), equalTo(Status.WAITING));
        assertThat(booking.getItem(), equalTo(item1));
        assertThat(booking.getBooker(), equalTo(user1));
        assertThat(booking.getStart(), notNullValue());
        assertThat(booking.getEnd(), notNullValue());
    }

    @Test
    public void test3_changeStatus() {
        Booking booking = bookingService.createBooking(user1.getId(), bookingDto);
        assertThat(booking.getStatus(), equalTo(Status.WAITING));
        booking = bookingService.changeStatus(booking.getId(), false, user2.getId());
        assertThat(booking.getStatus(), equalTo(Status.REJECTED));
        booking = bookingService.changeStatus(booking.getId(), true, user2.getId());
        assertThat(booking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    public void test4_getAllBooking() {
        Booking booking = bookingService.createBooking(user1.getId(), bookingDto);
        List<Booking> bookingList = bookingService.getAllBooking(new BookingDtoState(
                        user1.getId(), State.ALL),
                0,
                5
        );
        assertThat(bookingList.get(0), equalTo(booking));
    }

    @Test
    public void test5_getAllBookingByOwner() {
        Booking booking = bookingService.createBooking(user1.getId(), bookingDto);
        List<Booking> bookingList = bookingService.getAllBookingByOwner(
                new BookingDtoState(user2.getId(), State.ALL),
                0,
                5
        );
        assertThat(bookingList.get(0), equalTo(booking));
    }
}