package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // Ниже начинается новая функциональность которую нужно добавить
    // TODO: начать добавлять новую функциональность
    //     Доделать:
    //         - сообщение о том, что этот пользователь уже добавлен/удален из друзей ✅
    //         - проверка при удалении друга, когда его нет (множество пустое) или такого пользователя не существует?
    //     Сделать:
    //         - возвращаем список пользователей, являющихся его друзьями.
    //         - список друзей, общих с другим пользователем..
    //         - всё остальное...

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) {
            log.error("Пользователь с id = " + id + " не найден");
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }

    @Override
    public User addFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user.getFriends() == null) {
            user.setFriends();
        }
        if (friend.getFriends() == null) {
            friend.setFriends();
        }
        if (user.getFriends().contains(friendId)) {
            log.error("Попытка повторного добавления пользователя с id = " + friendId + " пользователем id = " + userId);
            throw new ValidationException("Пользователь с id = " + friendId + " уже имеется в друзьях");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        return user;
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user.getFriends() == null) {
            throw new ValidationException("У вас нет ");
        }
        if (!user.getFriends().contains(friendId)) {
            log.error("Попытка повторного удаления пользователя с id = " + friendId + " пользователем id = " + userId);
            throw new ValidationException("Пользователь с id = " + friendId + " уже удален из друзей");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        return user;
    }

    @Override
    public Set<Integer> getFriends(int userId) {
        return users.get(userId).getFriends();
    }

    @Override
    public List<User> getCommonFriend(int otherUserId) {
        return null;
    }
}
