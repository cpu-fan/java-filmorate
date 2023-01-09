package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
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

@Component("filmDbStorageDaoImpl")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingDao mpaRatingDao;
    private final GenreDaoImpl genreDao;

    private final UserDbStorage userDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         MpaRatingDao mpaRatingDao,
                         GenreDaoImpl genreDao,
                         UserDbStorage userDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRatingDao = mpaRatingDao;
        this.genreDao = genreDao;
        this.userDbStorage = userDbStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, this::mapRowFilm);
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue(); // filmId нового фильма
        // Добавляем в film_genres фильм и его жанры
        genreDao.addFilmGenres(filmId, film.getGenres());
        // Возвращаем в ответ тот же фильм, но с добавлением названий жанра и рейтинга MPA
        return getFilmWithMpaAndGenreNames(filmId, film);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "where id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        checkAndUpdateGenres(film); // проверяем и обновляем по необходимости список жанров фильма
        return getFilmWithMpaAndGenreNames(film.getId(), film);
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
        String sql = "insert into film_likes values (?, ?)";
        boolean isLiked = checksForLikes(filmId, userId);
        if (isLiked) {
            log.error(String.format("Пользователь id = %d уже лайкал фильм id = %d", userId, filmId));
            throw new ValidationException(String.format("Пользователь id = %d уже лайкал фильм id = %d",
                    userId, filmId));
        }
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId); // снова вызываем этот метод для возврата фильма, но уже с лайком
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        String sql = "delete from film_likes where film_id = ? and user_id = ?";
        boolean isLiked = checksForLikes(filmId, userId);
        if (!isLiked) {
            log.error(String.format("Пользователь id = %d уже удалил лайк к фильму id = %d", userId, filmId));
            throw new ValidationException(String.format("Пользователь id = %d уже удалил лайк к фильму id = %d",
                    userId, filmId));
        }
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "select * from FILMS where id in (select FILM_ID from FILM_LIKES group by FILM_ID " +
                "having count(USER_ID) order by count(USER_ID) desc) limit ?";
        return jdbcTemplate.query(sql, this::mapRowFilm, count);
    }

    private Film mapRowFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("id");
        int mpaRatingId = rs.getInt("mpa_rating_id");
        Set<Genre> filmGenres = genreDao.getFilmGenres(filmId);
        MpaRating mpaRating = mpaRatingDao.getMpaRatingById(mpaRatingId);
        Set<Integer> filmLikes = getFilmLikes(filmId);

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .likes(filmLikes)
                .mpa(mpaRating)
                .genres(filmGenres)
                .build();
    }

    private Film getFilmWithMpaAndGenreNames(int filmId, Film film) {
        // Получаем жанры и MPA-рейтинг с названиями из БД для ответа, т.к. по API приходят только их id.
        Set<Genre> filmGenres = genreDao.getFilmGenres(filmId);
        MpaRating mpaRating = mpaRatingDao.getMpaRatingById(film.getMpa().getId());

        return film.toBuilder()
                .id(filmId)
                .genres(filmGenres)
                .mpa(mpaRating)
                .build();
    }

    private void checkAndUpdateGenres(Film film) {
        int filmId = film.getId();
        Set<Genre> filmGenres = film.getGenres();
        Set<Genre> filmGenresFromDB = genreDao.getFilmGenres(filmId);
        // Если список жанров у обновляемого фильма различается с тем, что в БД, то обновляем список жанров
        if (!filmGenres.equals(filmGenresFromDB)) {
            genreDao.deleteFilmGenres(filmId);
            genreDao.addFilmGenres(filmId, filmGenres);
        }
    }

    private Set<Integer> getFilmLikes(int filmId) {
        String sql = "select user_id from film_likes where film_id = ?";
        List<Integer> listLikes = jdbcTemplate.query(sql, this::mapRowLikes, filmId);
        return new HashSet<>(listLikes);
    }

    private int mapRowLikes(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("user_id");
    }

    private boolean checksForLikes(int filmId, int userId) {
        String sql = "select * from film_likes where film_id = ? and user_id = ?";
        // проверяем есть ли такой фильм и пользователь
        getFilmById(filmId);
        userDbStorage.getUserById(userId);
        // запрос для проверки, имеются ли строки в БД
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, filmId, userId);
        sqlRowSet.last();
        return sqlRowSet.getRow() > 0;
    }
}
