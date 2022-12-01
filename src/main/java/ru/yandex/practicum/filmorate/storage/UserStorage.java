package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserStorage {
    int generateUserId();

    Map<Integer, User> getUsers();

    User addUser(User user);

    void updateUser(int id, User user);

    User getUserById(int id);

    User addFriend(int userId, int friendId);

    User deleteFriend(int userId, int friendId);

    Set<Integer> getFriends(int userId);

    List<User> getCommonFriend(int otherUserId);
}
