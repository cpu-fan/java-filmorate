package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return userStorage.getUsers().values();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User createUser(User user) {
        checkAndSetName(user);
        user = userStorage.addUser(user);
        log.info("Добавлен новый пользователь: " + user);
        return user;
    }

    public User updateUser(User user) {
        checkAndSetName(user);
        checkAndUpdateUser(user);
        log.info("Внесены обновления в информацию о пользователе id = " + user.getId());
        return user;
    }

    private void checkAndSetName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkAndUpdateUser(User user) {
        int id = user.getId();
        if (userStorage.getUsers().containsKey(id)) {
            userStorage.updateUser(id, user);
        } else {
            log.error("Пользователь с id = " + id + " не найден");
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    public User addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка добавления самого себя в друзья пользователя с id = " + userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        log.info("Пользователь с id = " + userId + " добавил в друзья пользователя с id = " + friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка удаления самого себя из друзей");
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        log.info("Пользователь с id = " + userId + " удалил из друзей пользователя с id = " + friendId);
        return userStorage.deleteFriend(userId, friendId);
    }
}
