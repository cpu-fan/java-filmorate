package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE);
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getFilms().values();
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
        if (filmStorage.getFilms().containsKey(id)) {
            filmStorage.updateFilm(id, film);
        } else {
            log.error("Фильм с id = " + id + " не найден");
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }
}
