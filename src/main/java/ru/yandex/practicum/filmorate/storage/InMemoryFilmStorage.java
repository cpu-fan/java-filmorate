package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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

    // Ниже начинается новая функциональность которую нужно добавить
    // TODO: начать добавлять новую функциональность
    @Override
    public void deleteFilm(int filmId) {

    }

    @Override
    public void addLike(int filmId, int userId) {

    }

    @Override
    public void deleteLike(int filmId, int userId) {

    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return null;
    }
}
