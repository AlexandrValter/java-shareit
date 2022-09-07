package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        log.info("Добавлен пользователь {}", user.getName());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(long userId, User user) {
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(String.format("Пользователь id = %s не найден", userId)));
        updateFields(user, updateUser);
        log.info("Обновлена информация о пользователе id = {}", userId);
        return userRepository.save(updateUser);
    }

    @Override
    public User getUser(long userId) {
        log.info("Запрошена информация о пользователе id = {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(String.format("Пользователь id = %s не найден", userId)));
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Запрошены все пользователи");
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Удален пользователь id = {}", userId);
        userRepository.deleteById(userId);
    }

    private void updateFields(User user, User updateUser) {
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            updateUser.setName(user.getName());
        }
    }
}