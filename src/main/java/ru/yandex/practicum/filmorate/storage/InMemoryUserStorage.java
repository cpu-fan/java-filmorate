package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
    @Override
    public void deleteUser(int userId) {

    }

    @Override
    public void addFriend(int userId, int friendId) {

    }

    @Override
    public void deleteFriend(int userId, int friendId) {

    }

    @Override
    public List<User> getFriends(int userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriend(int otherUserId) {
        return null;
    }
}
