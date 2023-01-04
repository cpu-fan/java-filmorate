package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreDao {

    List<Genre> getGenres();

    Genre getGenreById(int id);

    List<Genre> getFilmGenres(int filmId);

    void addFilmGenres(int filmId, Set<Genre> filmGenres);
}
