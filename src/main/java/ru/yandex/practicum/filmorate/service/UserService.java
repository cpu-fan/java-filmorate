package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    private int id;
    private Map<Integer, User> users = new HashMap<>();

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public User createUser(User user) {
        checkAndSetName(user);
        int id = generateId();
        user.setId(id);
        users.put(id, user);
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
        if (users.containsKey(id)) {
            users.put(id, user);
        } else {
            log.error("Пользователя с id = " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private int generateId() {
        return ++id;
    }
}
