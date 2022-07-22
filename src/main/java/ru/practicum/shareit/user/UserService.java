package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserService {
    User createUser(User user);

    User updateUser(long userId, User user);

    UserDto getUser(Long userId);

    Collection<UserDto> getAllUsers();

    void deleteUser(long userId);
}