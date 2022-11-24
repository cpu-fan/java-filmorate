package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FilmService {
    private int id;
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE);
    private Map<Integer, Film> films = new HashMap<>();

    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    public Film createFilm(Film film) {
        releaseCheck(film);
        int id = generateId();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен новый фильм: " + film);
        return film;
    }

    public Film updateFilm(Film film) {
        releaseCheck(film);
        checkAndUpdateFilm(film);
        log.info("Внесены обновления в информацию для фильма id = " + film.getId());
        return film;
    }

    private void releaseCheck(Film film) {
        if (film.getReleaseDate().isBefore(FilmService.MIN_DATE_RELEASE)) {
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

    private int generateId() {
        return ++id;
    }
}
