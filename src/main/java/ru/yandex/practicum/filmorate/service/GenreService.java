package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.impl.GenreDaoImpl;

import java.util.List;

@Service
@Slf4j
public class GenreService {
    private final GenreDao genreDaoImpl;

    @Autowired
    public GenreService(GenreDaoImpl genreDaoImpl) {
        this.genreDaoImpl = genreDaoImpl;
    }

    public List<Genre> getGenres() {
        log.info("Запрошен список жанров");
        return genreDaoImpl.getGenres();
    }

    public Genre getGenreById(int id) {
        log.info("Запрошен жанр id = " + id);
        return genreDaoImpl.getGenreById(id);
    }
}
