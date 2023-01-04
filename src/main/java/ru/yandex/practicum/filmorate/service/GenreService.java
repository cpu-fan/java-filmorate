package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.impl.GenreDaoImpl;

import java.util.List;

@Service
public class GenreService {
    private final GenreDaoImpl genreDaoImpl;

    @Autowired
    public GenreService(GenreDaoImpl genreDaoImpl) {
        this.genreDaoImpl = genreDaoImpl;
    }

    public List<Genre> getGenres() {
        return genreDaoImpl.getGenres();
    }

    public Genre getGenreById(int id) {
        return genreDaoImpl.getGenreById(id);
    }
}
