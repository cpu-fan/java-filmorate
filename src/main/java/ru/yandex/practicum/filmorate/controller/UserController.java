package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        checkAndSetName(user);
        int id = users.size() + 1;
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен новый пользователь: " + user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
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
}
