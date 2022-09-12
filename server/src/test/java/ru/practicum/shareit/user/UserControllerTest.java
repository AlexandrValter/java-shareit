package ru.practicum.shareit.user;

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

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final User user = new User(1L, "Test", "test@test.com");

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    public void test1_tryCreateUser() {
        User userBody = new User();
        userBody.setName("Test");
        userBody.setEmail("test@test.com");
        when(userService.createUser(Mockito.any(User.class)))
                .thenReturn(user);
        try {
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userBody))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is("Test")))
                    .andExpect(jsonPath("$.email", is("test@test.com")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test2_tryUpdateUser() {
        User update = new User();
        update.setName("Update");
        update.setEmail("update@yandex.ru");
        User userAfterUpdate = new User(1L, "Update", "update@yandex.ru");
        when(userService.updateUser(Mockito.anyLong(), Mockito.any(User.class)))
                .thenReturn(userAfterUpdate);
        try {
            mvc.perform(patch("/users/{userId}", 1)
                            .content(mapper.writeValueAsString(update))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is("Update")))
                    .andExpect(jsonPath("$.email", is("update@yandex.ru")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test3_tryGetUserById() {
        when(userService.getUser(Mockito.anyLong()))
                .thenReturn(user);
        try {
            mvc.perform(get("/users/{userId}", 1)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                    .andExpect(jsonPath("$.name", is("Test")))
                    .andExpect(jsonPath("$.email", is("test@test.com")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test4_tryGetAllUsers() {
        User user1 = new User(2L, "New name", "new@ya.ru");
        List<User> userList = List.of(user, user1);
        when(userService.getAllUsers())
                .thenReturn(userList);
        try {
            mvc.perform(get("/users")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.*", is(hasSize(2))))
                    .andExpect(jsonPath("$.[0].id", is(1)))
                    .andExpect(jsonPath("$.[1].id", is(2)))
                    .andExpect(jsonPath("$.[0].name", is("Test")))
                    .andExpect(jsonPath("$.[1].name", is("New name")))
                    .andExpect(jsonPath("$.[0].email", is("test@test.com")))
                    .andExpect(jsonPath("$.[1].email", is("new@ya.ru")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void test5_tryDeleteUserById() {
        try {
            mvc.perform(delete("/users/{userId}", 1)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}