package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    private BookingService bookingService;
    @InjectMocks
    private BookingController controller;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final User user = new User(1L, "User", "user@ya.ru");
    private final Item item = new Item(1L, "Item", "Item for test", true);
    private final BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
    );
    private final BookingDto bookingDto2 = new BookingDto(
            1L,
            LocalDateTime.now().plusDays(3),
            LocalDateTime.now().plusDays(5)
    );
    private final Booking booking1 = BookingMapper.toBooking(bookingDto);
    private final Booking booking2 = BookingMapper.toBooking(bookingDto2);

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        mapper.findAndRegisterModules();
        booking1.setItem(item);
        booking1.setBooker(user);
        booking1.setStatus(Status.WAITING);
        booking1.setId(1L);
        booking2.setItem(item);
        booking2.setBooker(user);
        booking2.setStatus(Status.APPROVED);
        booking2.setId(1L);
    }

    @Test
    public void test1_createBooking() {
        when(bookingService.createBooking(Mockito.anyLong(), Mockito.any(BookingDto.class)))
                .thenReturn(booking1);
        try {
            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(bookingDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(booking1)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test2_tryGetBooking() {
        when(bookingService.getBooking(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(booking1);
        try {
            mvc.perform(get("/bookings/{bookingId}", 1L)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(booking1)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test3_tryChangeStatus() {
        booking1.setStatus(Status.APPROVED);
        when(bookingService.changeStatus(booking1.getId(), true, user.getId()))
                .thenReturn(booking1);
        try {
            mvc.perform(patch("/bookings/{bookingId}", 1L)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1)
                            .param("approved", "true"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(booking1)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test4_tryGetAllBooking() {
        when(bookingService.getAllBooking(Mockito.any(BookingDtoState.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(booking1, booking2));
        try {
            mvc.perform(get("/bookings")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1)
                            .param("from", "0")
                            .param("state", "ALL")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.*", is(hasSize(2))))
                    .andExpect(content().json(mapper.writeValueAsString(List.of(booking1, booking2))));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test5_tryGetAllBookingByOwner() {
        when(bookingService.getAllBookingByOwner(Mockito.any(BookingDtoState.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(booking1, booking2));
        try {
            mvc.perform(get("/bookings/owner")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1)
                            .param("from", "0")
                            .param("state", "ALL")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.*", is(hasSize(2))))
                    .andExpect(content().json(mapper.writeValueAsString(List.of(booking1, booking2))));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}