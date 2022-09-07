package ru.practicum.shareit.item;

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
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController controller;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ItemDto itemDto = new ItemDto(1L, "Test_item1", "Item for test", true);
    private final User user = new User(1L, "Test_user", "test@ya.ru");
    private final ItemDtoWithBooking item1 = ItemMapper.toItemDtoWithBooking(new Item(
            1L,
            "Item1",
            "Item1 for test",
            true)
    );
    private final ItemDtoWithBooking item2 = ItemMapper.toItemDtoWithBooking(new Item(
            2L,
            "Item2",
            "Item2 for test",
            true)
    );
    private final CommentDtoFromRequest commentBody = new CommentDtoFromRequest();

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    public void test1_tryCreateItem() {
        when(itemService.createItem(Mockito.any(ItemDto.class), Mockito.anyLong()))
                .thenReturn(ItemMapper.toItem(itemDto));
        try {
            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(itemDto.getName())))
                    .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                    .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test2_tryCreateNotValidItem() {
        ItemDto emptyName = new ItemDto(1L, "", "description", true);
        ItemDto nullDescription = new ItemDto();
        nullDescription.setId(2L);
        nullDescription.setName("Name");
        nullDescription.setAvailable(false);
        ItemDto nullAvailable = new ItemDto();
        nullAvailable.setId(3L);
        nullAvailable.setName("Name");
        nullAvailable.setDescription("Description");
        try {
            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(emptyName))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(nullDescription))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(nullAvailable))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test3_tryUpdateItem() {
        ItemDto update = new ItemDto();
        update.setName("Update");
        update.setDescription("Update");
        update.setRequestId(1L);
        update.setAvailable(false);
        ItemDto itemAfterUpdate = new ItemDto(1L, "Update", "Update", false);
        when(itemService.updateItem(Mockito.anyLong(), Mockito.any(ItemDto.class), Mockito.anyLong()))
                .thenReturn(ItemMapper.toItem(itemAfterUpdate));
        try {
            mvc.perform(patch("/items/{itemId}", 1)
                            .content(mapper.writeValueAsString(update))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(itemAfterUpdate.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is(itemAfterUpdate.getName())))
                    .andExpect(jsonPath("$.description", is(itemAfterUpdate.getDescription())))
                    .andExpect(jsonPath("$.available", is(itemAfterUpdate.getAvailable())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test4_tryGetItem() {
        when(itemService.getItem(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(item1);
        try {
            mvc.perform(get("/items/{itemId}", 1)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(item1)));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test5_tryGetAllItems() {
        when(itemService.getAllItem(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(item1, item2));
        try {
            mvc.perform(get("/items", 1)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(item1, item2))));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test6_trySearchItems() {
        when(itemService.searchItems(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(ItemMapper.toItem(itemDto)));
        String text = "Test_item1";
        try {
            mvc.perform(get("/items/search")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1)
                            .param("text", text))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test7_tryCreateComment() {
        Comment comment = new Comment();
        comment.setText("Comment text");
        comment.setItem(ItemMapper.toItem(itemDto));
        comment.setAuthor(new User(1L, "Author", "author@yandex.ru"));
        comment.setId(1L);
        commentBody.setText("Comment text");
        when(itemService.createComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentDtoFromRequest.class)))
                .thenReturn(comment);
        try {
            mvc.perform(post("/items/{itemId}/comment", 1)
                            .content(mapper.writeValueAsString(commentBody))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                    .andExpect(jsonPath("$.text", is(comment.getText())))
                    .andExpect(jsonPath("$.authorName", is(comment.getAuthor().getName())))
                    .andExpect(jsonPath("$.created", is(notNullValue())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test8_tryCreateCommentWithIncorrectText() {
        try {
            mvc.perform(post("/items/{itemId}/comment", 1)
                            .content(mapper.writeValueAsString(commentBody))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().is(400));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        commentBody.setText("");
        try {
            mvc.perform(post("/items/{itemId}/comment", 1)
                            .content(mapper.writeValueAsString(commentBody))
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