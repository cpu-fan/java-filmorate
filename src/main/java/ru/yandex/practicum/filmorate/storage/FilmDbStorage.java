package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.MpaRatingDao;
import ru.yandex.practicum.filmorate.storage.impl.GenreDaoImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("filmDaoImpl")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingDao mpaRatingDao;
    private final GenreDaoImpl genreDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaRatingDao mpaRatingDao, GenreDaoImpl genreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRatingDao = mpaRatingDao;
        this.genreDao = genreDao;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, this::mapRowFilm);
    }

    // TODO: надо подумать как лучше сделать, чтобы в ответ добавлять еще и имена жанров/рейтинга, т.к. сейчас
    //  приходит null. Пример:
    //                            "genres": [
    //                                {
    //                                    "id": 1,
    //                                    "name": null
    //                                }
    //                            ],
    //                            "mpa": {
    //                                "id": 3,
    //                                "name": null
    //                            }
    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        genreDao.addFilmGenres(id, film.getGenres());

        return film.toBuilder().id(id).build();
    }

    @Override
    public void updateFilm(int id, Film film) {
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "select * from films where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowFilm, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.format("Фильм с id = %d не существует", id));
            throw new NotFoundException(String.format("Фильм с id = %d не существует", id));
        }
    }

    @Override
    public Film addLike(int filmId, int userId) {
        return null;
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        return null;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return null;
    }

    private Film mapRowFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("id");
        int mpaRatingId = rs.getInt("mpa_rating_id");
        Set<Genre> filmGenres = new HashSet<>(genreDao.getFilmGenres(filmId));
        MpaRating mpa = mpaRatingDao.getMpaRatingById(mpaRatingId);

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .genres(filmGenres)
                .build();
    }
}
