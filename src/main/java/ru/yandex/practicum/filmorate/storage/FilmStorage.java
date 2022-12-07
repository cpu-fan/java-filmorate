package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {

    int generateFilmId();

    Map<Integer, Film> getFilms();

    Film addFilm(Film film);

    void updateFilm(int id, Film film);

    Film getFilmById(int id);

    Film addLike(int filmId, int userId);

    Film deleteLike(int filmId, int userId);

    List<Film> getPopularFilms(int count);
}
