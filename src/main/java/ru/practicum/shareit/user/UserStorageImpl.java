package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id;

    @Override
    public User addUser(User user) {
        if (validateEmail(user)) {
            throw new DuplicateEmailException(
                    String.format("Пользователь с e-mail %s уже существует",
                            user.getEmail()));
        }
        user.setId(makeId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь id={}", user.getId());
        return users.get(user.getId());
    }

    @Override
    public User updateUser(long userId, User user) {
        User newUser = new User();
        if (users.containsKey(userId)) {
            newUser = users.get(userId);
        }
        if (user.getName() != null) {
            newUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            if (validateEmail(user)) {
                throw new DuplicateEmailException(
                        String.format("Пользователь с e-mail %s уже существует",
                                user.getEmail()));
            }
            newUser.setEmail(user.getEmail());
        }
        users.put(userId, newUser);
        log.info("Обновлена информация о пользователе id={}", userId);
        return newUser;
    }

    @Override
    public UserDto getUser(Long userId) {
        if (users.containsKey(userId)) {
            log.info("Запрошена информация о пользователе id={}", userId);
            return UserMapper.toUserDto(users.get(userId));
        } else {
            throw new NotFoundUserException(String.format("Пользователь с id %s не найден", userId));
        }
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Удален пользователь id={}", userId);
        users.remove(userId);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        log.info("Запрошены все пользователи");
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private boolean validateEmail(User user) {
        return users.values().stream()
                .map(User::getEmail)
                .anyMatch(user.getEmail()::equals);
    }

    private Long makeId() {
        return (id == null) ? id = 1L : ++id;
    }
}