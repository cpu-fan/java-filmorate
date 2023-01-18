package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.sql.PreparedStatement;
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

    public List<Genre> getGenres(List<Integer> filmIds) {
        // TODO: проработать этот метод через updateBatch
        String sql = "select * from genres where film_id in (?)";
        return jdbcTemplate.query(sql, this::mapRowGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "select * from genres where id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowGenre, id);
        if (genres.isEmpty()) {
            log.error(String.format("Жанр с id = %d не существует", id));
            throw new NotFoundException(String.format("Жанр с id = %d не существует", id));
        }
        return genres.get(0);
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

        jdbcTemplate.batchUpdate(sql, filmGenres, 50, (PreparedStatement ps, Genre genre) -> {
            ps.setInt(1, filmId);
            ps.setInt(2, genre.getId());
        });
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
