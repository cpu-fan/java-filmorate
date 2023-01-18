package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public FilmService(@Qualifier("filmDbStorageDaoImpl") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        log.info("Запрошен список фильмов");
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        log.info("Запрошен фильм id = " + id);
        return filmStorage.getFilmById(id);
    }

    public Film createFilm(Film film) {
        releaseCheck(film);
        film = filmStorage.addFilm(film);
        log.info("Добавлен новый фильм: " + film);
        return film;
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());
        releaseCheck(film);
        log.info("Внесены обновления в информацию для фильма id = " + film.getId());
        return filmStorage.updateFilm(film);
    }

    private void releaseCheck(Film film) {
        if (film.getReleaseDate().isBefore(FilmService.MIN_DATE_RELEASE)) {
            log.error("У фильма " + film + " дата релиза раньше возможной даты " + MIN_DATE_RELEASE);
            throw new ValidationException("Дата релиза фильма не может быть раньше чем 28 декабря 1895");
        }
    }

    public Film addLike(int filmId, int userId) {
        log.info("Добавление лайка фильму id = " + filmId + " пользователем id = " + userId);
        userService.getUserById(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        log.info("Удаление лайка у фильма id = " + filmId + " пользователем id = " + userId);
        userService.getUserById(userId);
        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.error("Недопустимое значение count: " + count);
            throw new ValidationException("Недопустимое значение count: " + count);
        }
        log.info("Запрошен список топ-" + count + " популярных фильмов");
        return filmStorage.getPopularFilms(count);
    }
}
