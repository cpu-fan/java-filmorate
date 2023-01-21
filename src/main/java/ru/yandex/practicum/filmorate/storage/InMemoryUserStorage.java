package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component("userInMemoryImpl")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    public int generateUserId() {
        return ++id;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User addUser(User user) {
        user.setId(generateUserId());
        users.put(id, user);
        return users.get(id);
    }

    @Override
    public User updateUser(User user) {
        int id = user.getId();
        users.put(id, user);
        return users.get(id); // обновил этот метод после изменения в интерфейсе с void на User
    }

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) {
            log.error("Пользователя с id = " + id + " не существует");
            throw new NotFoundException("Пользователя с id = " + id + " не существует");
        }
        return user;
    }

    @Override
    public User addFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (userId == friendId) {
            log.error("Переданы одинаковые userId и friendId");
            throw new ValidationException("Переданы одинаковые userId и friendId");
        }
        if (user.getFriends().contains(friendId)) {
            log.error("Попытка повторного добавления пользователя с id = " + friendId + " пользователем id = " + userId);
            throw new ValidationException("Пользователь с id = " + friendId + " уже имеется в друзьях");
        }
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("Пользователь с id = " + userId + " добавил в друзья пользователя с id = " + friendId);
        return user;
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (userId == friendId) {
            log.error("Переданы одинаковые userId и friendId");
            throw new ValidationException("Переданы одинаковые userId и friendId");
        }
        if (!user.getFriends().contains(friendId)) {
            log.error("Пользователя с id = " + friendId + " нет в списке друзей пользователя id = " + userId);
            throw new NotFoundException("Пользователя с id = " + friendId + " нет в списке ваших друзей");
        }
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.info("Пользователь с id = " + userId + " удалил из друзей пользователя с id = " + friendId);
        return user;
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserById(userId);
        Set<Integer> otherUserFriends = getUserById(otherId).getFriends();
        return user.getFriends().stream()
                .filter(otherUserFriends::contains)
                .map(users::get)
                .collect(Collectors.toList());
    }
}
