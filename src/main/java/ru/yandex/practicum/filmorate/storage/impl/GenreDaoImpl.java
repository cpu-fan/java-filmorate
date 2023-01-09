package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class GenreDaoImpl implements GenreDao {

    private final JdbcTemplate jdbcTemplate;

    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "select * from genres";
        return jdbcTemplate.query(sql, this::mapRowGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "select * from genres where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowGenre, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.format("Жанр с id = %d не существует", id));
            throw new NotFoundException(String.format("Жанр с id = %d не существует", id));
        }
    }

    @Override
    public Set<Genre> getFilmGenres(int filmId) {
        String sql = "select * from genres where id in (select genre_id from film_genres where film_id = ?)";
        List<Genre> genreList = jdbcTemplate.query(sql, this::mapRowGenre, filmId);
        return new HashSet<>(genreList);
    }

    @Override
    public void addFilmGenres(int filmId, Set<Genre> filmGenres) {
        String sql = "insert into film_genres values (?, ?)";
        for (Genre filmGenre : filmGenres) {
            jdbcTemplate.update(sql, filmId, filmGenre.getId());
        }
    }

    public void deleteFilmGenres(int filmId) {
        String sql = "delete from film_genres where film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Genre mapRowGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
