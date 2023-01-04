package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.MpaRatingDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class MpaRatingDaoImpl implements MpaRatingDao {

    private final JdbcTemplate jdbcTemplate;

    public MpaRatingDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> getMpaRatings() {
        String sql = "select * from mpa_ratings";
        return jdbcTemplate.query(sql, this::mapRowMpaRating);
    }

    @Override
    public MpaRating getMpaRatingById(int id) {
        String sql = "select * from mpa_ratings where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowMpaRating, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.format("MPA-рейтинг с id = %d не существует", id));
            throw new NotFoundException(String.format("MPA-рейтинг с id = %d не существует", id));
        }
    }

    private MpaRating mapRowMpaRating(ResultSet rs, int rowNum) throws SQLException {
        return MpaRating.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
