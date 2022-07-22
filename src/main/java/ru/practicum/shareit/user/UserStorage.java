package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    User updateUser(long userId, User user);

    UserDto getUser(Long userId);

    void deleteUser(long userId);

    Collection<UserDto> getAllUsers();
}