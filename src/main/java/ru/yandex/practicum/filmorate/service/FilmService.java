package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE);
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms().values();
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        return film;
    }

    public Film createFilm(Film film) {
        releaseCheck(film);
        film = filmStorage.addFilm(film);
        log.info("Добавлен новый фильм: " + film);
        return film;
    }

    public Film updateFilm(Film film) {
        releaseCheck(film);
        checkAndUpdateFilm(film);
        log.info("Внесены обновления в информацию для фильма id = " + film.getId());
        return film;
    }

    private void releaseCheck(Film film) {
        if (film.getReleaseDate().isBefore(FilmService.MIN_DATE_RELEASE)) {
            log.error("У фильма " + film + " дата релиза раньше возможной даты " + MIN_DATE_RELEASE);
            throw new ValidationException("Дата релиза фильма не может быть раньше чем 28 декабря 1895");
        }
    }

    private void checkAndUpdateFilm(Film film) {
        int id = film.getId();
        if (filmStorage.getFilms().containsKey(id)) {
            filmStorage.updateFilm(id, film);
        } else {
            log.error("Фильм с id = " + id + " не найден");
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    public Film addLike(int filmId, int userId) {
        // Здесь и в методе deleteLike() изначально я сделал такую проверку, что если приходит недопустимый id, то
        // выбрасываю ошибку валидации.
//        if (userId <= 0) {
//            log.error("Недопустимое значение id пользователя: " + userId);
//            throw new ValidationException("Недопустимое значение id пользователя: " + userId);
//        }
        // Но потом, я в тестах увидел, что в запросе удаления лайка с некорректным userId ожидается 404, а не 400.
        // Получается, что FilmServices должен быть связан с UserServices? Через внедрение? В ТЗ об этом вроде не
        // говорили и не намекали, поэтому я подумал, что будет достаточно сделать проверку на корректность id.
        // Но все же в итоге внедрил UserService сюда, и для проверки есть ли такой юзер (userId) просто вызываю метод
        // getUserById(userId), в котором уже имеется проверка на существование юзера.
        // Не знаю конечно насколько это правильно. :)
        userService.getUserById(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        userService.getUserById(userId);
        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.error("Недопустимое значение count: " + count);
            throw new ValidationException("Недопустимое значение count: " + count);
        }
        return filmStorage.getPopularFilms(count);
    }
}
