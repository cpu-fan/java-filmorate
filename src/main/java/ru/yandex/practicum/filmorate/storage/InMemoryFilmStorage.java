package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component("filmInMemoryImpl")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private int id;
    private final Map<Integer, Film> films = new HashMap<>();

    public int generateFilmId() {
        return ++id;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(generateFilmId());
        films.put(id, film);
        return films.get(id);
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = films.get(id);
        if (film == null) {
            log.error("Фильм с id = " + id + " не существует");
            throw new NotFoundException("Фильм с id = " + id + " не существует");
        }
        return film;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        film.addLike(userId);
        return film;
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        film.removeLike(userId);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(f -> f.getLikes().size() * -1))
                .limit(count)
                .collect(Collectors.toList());
    }
}
