package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private User user1 = new User(1L, "Test_name_1", "test1@email.com");

    @Test
    public void test1_tryCreateNewUser() {
        Mockito
                .when(userRepository.save(Mockito.eq(user1)))
                .thenReturn(user1);
        User newUser = userService.createUser(user1);
        Assertions.assertEquals(user1, newUser);
    }

    @Test
    public void test2_tryUpdateNewUser() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        User user2 = user1;
        user2.setEmail("new_email@email.com");
        user2.setName("New_name_for_test");
        Mockito
                .when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user2);
        User newUser = new User();
        newUser.setEmail("new_email@email.com");
        newUser.setName("New_name_for_test");
        User updateUser = userService.updateUser(1L, newUser);
        Assertions.assertEquals(1L, updateUser.getId());
        Assertions.assertEquals(newUser.getName(), updateUser.getName());
        Assertions.assertEquals(newUser.getEmail(), updateUser.getEmail());
    }

    @Test
    public void test3_tryUpdateNewUserWhenUserIdIsIncorrect() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> userService.updateUser(1L, new User()));
        Assertions.assertEquals("Пользователь id = 1 не найден", ex.getMessage());
    }

    @Test
    public void test4_tryGetUserById() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        User user = userService.getUser(1L);
        Assertions.assertEquals(user1.getId(), user.getId());
        Assertions.assertEquals(user1.getName(), user.getName());
        Assertions.assertEquals(user1.getEmail(), user.getEmail());
    }

    @Test
    public void test5_tryGetUserByIdWhenUserIdIsIncorrect() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundUserException ex = Assertions.assertThrows(NotFoundUserException.class,
                () -> userService.getUser(1L));
        Assertions.assertEquals("Пользователь id = 1 не найден", ex.getMessage());
    }

    @Test
    public void test6_tryGetAllUsers() {
        User user2 = new User(2L, "Test_name_2", "test2@email.com");
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));
        List<User> users = userService.getAllUsers();
        Assertions.assertEquals(List.of(user1, user2), users);
    }

    @Test
    public void test7_tryDeleteUser() {
        userService.deleteUser(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(1L);
    }

    @Test
    public void test8_tryUpdateNewUserWithNullFields() {
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        User user2 = new User();
        Mockito
                .when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user1);
        User updateUser = userService.updateUser(1L, user2);
        Assertions.assertEquals(1L, user1.getId());
        Assertions.assertEquals(user1.getName(), updateUser.getName());
        Assertions.assertEquals(user1.getEmail(), updateUser.getEmail());
    }
}