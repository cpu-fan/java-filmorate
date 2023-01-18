package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

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
        String sql = "select f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "mr.name as mpa_rating_name " +
                "from films as f inner join mpa_ratings as mr on f.mpa_rating_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowFilm);
        setGenres(films);
        setLikes(films);
        return films;
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
        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        int filmId = film.getId();

        String sql = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "where id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                filmId);

        checkAndUpdateGenres(film); // проверяем и обновляем по необходимости список жанров фильма

        return getFilmById(filmId);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "select f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "mr.name as mpa_rating_name " +
                "from films as f inner join mpa_ratings as mr on f.mpa_rating_id = mr.id " +
                "where f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowFilm, id);
        if (films.isEmpty()) {
            log.error(String.format("Фильм с id = %d не существует", id));
            throw new NotFoundException(String.format("Фильм с id = %d не существует", id));
        }
        Film film = films.get(0);
        setGenres(Collections.singletonList(film));
        setLikes(Collections.singletonList(film));
        return film;
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
        String sql = "select fl.film_id as id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_rating_id, mr.name as mpa_rating_name " +
                "from film_likes as fl " +
                "inner join films as f on fl.film_id = f.id " +
                "inner join mpa_ratings as mr on fl.film_id = mr.id " +
                "group by fl.film_id " +
                "having count(fl.user_id) " +
                "order by count(fl.user_id) desc " +
                "limit ?";
        List<Film> popularFilms = jdbcTemplate.query(sql, this::mapRowFilm, count);
        if (popularFilms.isEmpty()) {
            return new ArrayList<>(getFilms());
        }
        setGenres(popularFilms);
        setLikes(popularFilms);
        return popularFilms;
    }

    private Film mapRowFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("id");
        MpaRating mpaRating = MpaRating.builder()
                .id(rs.getInt("mpa_rating_id"))
                .name(rs.getString("mpa_rating_name"))
                .build();

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaRating)
                .build();
    }

    // fixme:
    private void setGenres(List<Film> films) {
        for (Film film : films) {
            film.setGenres(genreDao.getFilmGenres(film.getId()));
        }
    }

    // fixme:
    private void setLikes(List<Film> films) {
        for (Film film : films) {
            film.setLikes(getFilmLikes(film.getId()));
        }
    }

    private Set<Integer> getFilmLikes(int filmId) {
        String sql = "select user_id from film_likes where film_id = ?";
        List<Integer> listLikes = jdbcTemplate.query(sql, this::mapRowLikes, filmId);
        return new HashSet<>(listLikes);
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

    private int mapRowLikes(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("user_id");
    }

    private boolean checksForLikes(int filmId, int userId) {
        String sql = "select * from film_likes where film_id = ? and user_id = ?";
        // запрос для проверки, имеются ли строки в БД
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, filmId, userId);
        sqlRowSet.last();
        return sqlRowSet.getRow() > 0;
    }
}
