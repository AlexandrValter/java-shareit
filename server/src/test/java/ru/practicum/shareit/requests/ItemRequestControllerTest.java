package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestService service;
    @InjectMocks
    private ItemRequestController controller;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final User user = new User(1L, "test user", "test@email.com");
    private final ItemRequest request1 = new ItemRequest(
            1L,
            "test1",
            user,
            LocalDateTime.now());
    private final ItemRequest request2 = new ItemRequest(
            2L,
            "test2",
            user,
            LocalDateTime.now());
    private final ItemDto item = new ItemDto(1L, "item", "item for test", true);

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    public void test1_createRequest() {
        ItemRequest requestBody = new ItemRequest();
        requestBody.setDescription("test1");
        when(service.createRequest(Mockito.anyLong(), any()))
                .thenReturn(request1);
        try {
            mvc.perform(post("/requests")
                            .content(mapper.writeValueAsString(requestBody))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(request1.getId()), Long.class))
                    .andExpect(jsonPath("$.description", is("test1")))
                    .andExpect(jsonPath("$.created", is(notNullValue())))
                    .andExpect(jsonPath("$.items", is(Matchers.empty())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test2_tryGetRequestsByOwner() {
        item.setRequestId(2L);
        List<ItemRequestDto> requests = Stream.of(request1, request2)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        requests.get(1).setItems(Set.of(item));
        when(service.getRequestsByOwner(anyLong()))
                .thenReturn(requests);
        try {
            mvc.perform(get("/requests")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.*", is(hasSize(2))))
                    .andExpect(jsonPath("$.[0].id", is(1)))
                    .andExpect(jsonPath("$.[1].id", is(2)))
                    .andExpect(jsonPath("$.[0].description", is("test1")))
                    .andExpect(jsonPath("$.[1].description", is("test2")))
                    .andExpect(jsonPath("$.[0].created", is(notNullValue())))
                    .andExpect(jsonPath("$.[1].created", is(notNullValue())))
                    .andExpect(jsonPath("$.[0].items", is(empty())))
                    .andExpect(jsonPath("$.[1].items[*].id", containsInAnyOrder(1)))
                    .andExpect(jsonPath("$.[1].items[*].description", containsInAnyOrder(item.getDescription())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test3_tryGetAllRequests() {
        item.setRequestId(2L);
        List<ItemRequestDto> requests = Stream.of(request1, request2)
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        requests.get(1).setItems(Set.of(item));
        when(service.getAllRequest(anyLong(), anyInt(), anyInt()))
                .thenReturn(requests);
        try {
            mvc.perform(get("/requests/all")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.*", is(hasSize(2))))
                    .andExpect(jsonPath("$.[0].id", is(1)))
                    .andExpect(jsonPath("$.[1].id", is(2)))
                    .andExpect(jsonPath("$.[0].description", is("test1")))
                    .andExpect(jsonPath("$.[1].description", is("test2")))
                    .andExpect(jsonPath("$.[0].created", is(notNullValue())))
                    .andExpect(jsonPath("$.[1].created", is(notNullValue())))
                    .andExpect(jsonPath("$.[0].items", is(empty())))
                    .andExpect(jsonPath("$.[1].items[*].id", containsInAnyOrder(1)))
                    .andExpect(jsonPath("$.[1].items[*].description", containsInAnyOrder(item.getDescription())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test4_tryGetRequestById() {
        item.setRequestId(2L);
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(request2);
        requestDto.setItems(Set.of(item));
        when(service.getRequestById(eq(1L), eq(2L)))
                .thenReturn(requestDto);
        try {
            mvc.perform(get("/requests/{requestId}", 2)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(request2.getId()), Long.class))
                    .andExpect(jsonPath("$.description", is("test2")))
                    .andExpect(jsonPath("$.created", is(notNullValue())))
                    .andExpect(jsonPath("$.items[*].id", containsInAnyOrder(1)))
                    .andExpect(jsonPath("$.items[*].name", containsInAnyOrder(item.getName())))
                    .andExpect(jsonPath("$.items[*].description", containsInAnyOrder(item.getDescription())))
                    .andExpect(jsonPath("$.items[*].available", containsInAnyOrder(item.getAvailable())))
                    .andExpect(jsonPath("$.items[*].requestId", containsInAnyOrder(2)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}