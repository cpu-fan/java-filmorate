package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private int id;
    private Map<Integer, Film> films = new HashMap<>();

    @Override
    public int generateFilmId() {
        return ++id;
    }

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(generateFilmId());
        films.put(id, film);
        return films.get(id);
    }

    @Override
    public void updateFilm(int id, Film film) {
        films.put(id, film);
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
        film.getLikes().add(userId);
        return film;
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        film.getLikes().remove(userId);
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
