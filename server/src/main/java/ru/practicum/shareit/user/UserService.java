package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(long userId, User user);

    User getUser(long userId);

    List<User> getAllUsers();

    void deleteUser(long userId);
}