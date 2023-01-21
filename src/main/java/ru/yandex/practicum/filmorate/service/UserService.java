package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userStorageDaoImpl") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return userStorage.getUsers();
    }

    public User getUserById(int userId) {
        log.info("Запрошен пользователь с id = " + userId);
        return userStorage.getUserById(userId);
    }

    public User createUser(User user) {
        checkAndSetName(user);
        user = userStorage.addUser(user);
        log.info("Добавлен новый пользователь: " + user);
        return user;
    }

    public User updateUser(User user) {
        checkAndSetName(user);
        int id = user.getId();
        userStorage.getUserById(id);
        log.info("Внесены обновления в информацию о пользователе id = " + id);
        return userStorage.updateUser(user);
    }

    public User addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Нельзя добавить самого себя в друзья");
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        log.info("Пользователь id = " + userId + " добавил в друзья пользователя id = " + friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(int userId, int friendId) {
        log.info("Пользователь id = " + userId + " удалил из друзей пользователя id = " + friendId);
        return userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        log.info("Запрошен список друзей пользователя id = " + userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.info("Запрошен список общих друзей пользователя id = " + userId + " и пользователя id = " + otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void checkAndSetName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
