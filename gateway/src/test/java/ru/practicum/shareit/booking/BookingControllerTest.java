package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    private BookingClient client;
    @InjectMocks
    private BookingController controller;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        mapper.findAndRegisterModules();
    }

    @Test
    public void test1_createBookingWhenBookingIsNotValid() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setStart(LocalDateTime.now().minusDays(5));
        try {
            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(bookingDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        bookingDto.setStart(LocalDateTime.now().plusDays(5));
        bookingDto.setEnd(LocalDateTime.now().minusDays(5));
        try {
            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(bookingDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}