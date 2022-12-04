package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private int id;
    private Map<Integer, User> users = new HashMap<>();

    @Override
    public int generateUserId() {
        return ++id;
    }

    @Override
    public Map<Integer, User> getUsers() {
        return users;
    }

    @Override
    public User addUser(User user) {
        user.setId(generateUserId());
        users.put(id, user);
        return users.get(id);
    }

    @Override
    public void updateUser(int id, User user) {
        users.put(id, user);
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
        if (user.getFriends().contains(friendId)) {
            log.error("Попытка повторного добавления пользователя с id = " + friendId + " пользователем id = " + userId);
            throw new ValidationException("Пользователь с id = " + friendId + " уже имеется в друзьях");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с id = " + userId + " добавил в друзья пользователя с id = " + friendId);
        return user;
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (!user.getFriends().contains(friendId)) {
            log.error("Пользователя с id = " + friendId + " нет в списке друзей пользователя id = " + userId);
            throw new NotFoundException("Пользователя с id = " + friendId + " нет в списке ваших друзей");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с id = " + userId + " удалил из друзей пользователя с id = " + friendId);
        return user;
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = getUserById(userId);
        return Stream.of(user.getFriends().toArray())
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);
        return Stream.of(user.getFriends().toArray())
                .filter(id -> Arrays.asList(otherUser.getFriends().toArray()).contains(id))
                .map(users::get)
                .collect(Collectors.toList());
    }
}
