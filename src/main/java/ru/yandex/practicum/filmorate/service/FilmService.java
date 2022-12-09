package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE);
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        return film;
    }

    public Film createFilm(Film film) {
        releaseCheck(film);
        film = filmStorage.addFilm(film);
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
            log.error("У фильма " + film + " дата релиза раньше возможной даты " + MIN_DATE_RELEASE);
            throw new ValidationException("Дата релиза фильма не может быть раньше чем 28 декабря 1895");
        }
    }

    private void checkAndUpdateFilm(Film film) {
        int id = film.getId();
        if (filmStorage.getFilmById(id) != null) {
            filmStorage.updateFilm(id, film);
        }
    }

    public Film addLike(int filmId, int userId) {
        userService.getUserById(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        userService.getUserById(userId);
        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.error("Недопустимое значение count: " + count);
            throw new ValidationException("Недопустимое значение count: " + count);
        }
        return filmStorage.getPopularFilms(count);
    }
}
