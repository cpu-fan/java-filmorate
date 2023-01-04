package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

@Component("userDaoImpl")
public class UserDbStorage implements UserStorage {

    @Override
    public Collection<User> getUsers() {
        return null;
    }

    @Override
    public User addUser(User user) {
        return null;
    }

    @Override
    public void updateUser(int id, User user) {

    }

    @Override
    public User getUserById(int id) {
        return null;
    }

    @Override
    public User addFriend(int userId, int friendId) {
        return null;
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        return null;
    }

    @Override
    public List<User> getFriends(int userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return null;
    }
}
