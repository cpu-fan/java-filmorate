package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE);
    private Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        releaseCheck(film);
        int id = films.size() + 1;
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен новый фильм: " + film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        releaseCheck(film);
        checkAndUpdateFilm(film);
        log.info("Внесены обновления в информацию для фильма id = " + film.getId());
        return film;
    }

    private void releaseCheck(Film film) {
        if (film.getReleaseDate().isBefore(FilmController.MIN_DATE_RELEASE)) {
            log.error("Дата релиза фильма не может быть меньше 28 декабря 1895");
            throw new ValidationException(HttpStatus.BAD_REQUEST);
        }
    }

    private void checkAndUpdateFilm(Film film) {
        int id = film.getId();
        if (films.containsKey(id)) {
            films.put(id, film);
        } else {
            log.error("Фильма с id = " + id + " не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
