package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.controller.utils.testadapters.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan("ru.yandex.practicum.filmorate")
@WebMvcTest(controllers = FilmController.class)
class FilmControllerTest {
    private Film film;
    private User user;
    private static final String URL = "http://localhost:8080/films";
    private static final String URL_USER = "http://localhost:8080/users";

    private static Gson gson;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUpBeforeAll() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @BeforeEach
    void setUp() throws Exception {
        film = Film.builder()
                .name("District 9")
                .description("My favorite movie")
                .releaseDate(LocalDate.parse("2009-08-13", DateTimeFormatter.ISO_DATE))
                .duration(112)
                .build();

        user = User.builder()
                .login("Shrimp")
                .name("Wikus Van De Merwe")
                .email("wikusvandemerwe@mcu.com")
                .birthday(LocalDate.parse("1973-11-27", DateTimeFormatter.ISO_DATE))
                .build();

        String filmStr = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userStr = mockMvc.perform(post(URL_USER)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        film = gson.fromJson(filmStr, Film.class);
        user = gson.fromJson(userStr, User.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        film.getLikes().clear();
        mockMvc.perform(put(URL)
                .contentType("application/json")
                .content(gson.toJson(film)));
    }

    @Test
    void shouldCreateFilm() throws Exception {
        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenFilmNameEmpty() throws Exception {
        film = film.toBuilder()
                .name("")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenFilmDescriptionLengthIs200Symbols() throws Exception {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, нуу")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenFilmDescriptionLengthIs199Symbols() throws Exception {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, ну")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenFilmDescriptionLengthIs201Symbols() throws Exception {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, нууу")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenFilmDateIs28Dec1895() throws Exception {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE))
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenFilmDateAfter28Dec1895() throws Exception {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-29", DateTimeFormatter.ISO_DATE))
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenFilmDateBefore28Dec1895() throws Exception {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-27", DateTimeFormatter.ISO_DATE))
                .build();

        mockMvc.perform(post(URL)
            .contentType("application/json")
            .content(gson.toJson(film)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenFilmDurationIsPositive() throws Exception {
        film = film.toBuilder()
                .duration(1)
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenFilmDurationIs0() throws Exception {
        film = film.toBuilder()
                .duration(0)
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenFilmDurationIsNegative() throws Exception {
        film = film.toBuilder()
                .duration(-1)
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatedFilms() throws Exception {
        film = film.toBuilder()
                .id(film.getId())
                .name("updated name")
                .description("updated desc")
                .releaseDate(LocalDate.parse("2001-11-11", DateTimeFormatter.ISO_DATE))
                .duration(111)
                .build();

        MvcResult mvcResult = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(film)))
                .andReturn();

        int actualStatusCode = mvcResult.getResponse().getStatus();
        String actualBody = mvcResult.getResponse().getContentAsString();
        Film actualFilm = gson.fromJson(actualBody, Film.class);

        assertEquals(200, actualStatusCode);
        assertEquals(film.getName(), actualFilm.getName());
        assertEquals(film.getDescription(), actualFilm.getDescription());
        assertEquals(film.getReleaseDate(), actualFilm.getReleaseDate());
        assertEquals(film.getDuration(), actualFilm.getDuration());
    }

    @Test
    void shouldGetFilms() throws Exception {
        mockMvc.perform(get(URL)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetFilmById() throws Exception {
        Film actualFilm = gson.fromJson(mockMvc.perform(get(URL + "/1")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString(), Film.class);
        film.setId(1);
        assertEquals(film, actualFilm);
    }

    @Test
    void shouldGet400WhenFilmByIdNotFound() throws Exception {
        mockMvc.perform(get(URL + "/-1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddLike() throws Exception {
        Film actualFilm = gson.fromJson(mockMvc.perform(put(URL + "/1/like/1")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString(), Film.class);
        film.getLikes().add(1);

        assertEquals(film.getLikes().toArray()[0], actualFilm.getLikes().toArray()[0]);
    }

    @Test
    void shouldGet404WhenAddLikeAndFilmNotFound() throws Exception {
        mockMvc.perform(put(URL + "/-1/like/1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet404WhenAddLikeAndUserNotFound() throws Exception {
        mockMvc.perform(put(URL + "/1/like/-1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteLike() throws Exception {
        mockMvc.perform(put(URL + "/1/like/1")
                .contentType("application/json"))
                .andExpect(status().isOk());
        Film actualFilm = gson.fromJson(mockMvc.perform(delete(URL + "/1/like/1")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString(), Film.class);

        assertEquals(film.getLikes().isEmpty(), actualFilm.getLikes().isEmpty());
    }

    @Test
    void shouldGet404WhenDeleteLikeAndFilmNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/-1/like/1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet404WhenDeleteLikeAndUserNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/1/like/-1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPopularFilms() throws Exception {
        // создаем еще один фильм
        Film newFilm = Film.builder()
                .name("Inception")
                .description("One of my favorite films.")
                .releaseDate(LocalDate.parse("2010-07-22", DateTimeFormatter.ISO_DATE))
                .duration(148)
                .build();
        // создаем второго юзера
        User newUser = User.builder()
                .login("Top")
                .name("Cobb")
                .email("cobb@good-dreams.com")
                .birthday(LocalDate.parse("1974-11-11", DateTimeFormatter.ISO_DATE))
                .build();
        // отправляем их в апи
        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(newFilm)));
        mockMvc.perform(post(URL_USER)
                .contentType("application/json")
                .content(gson.toJson(newUser)));
        // ставим лайки
        mockMvc.perform(put(URL + "/1/like/1")
                .contentType("application/json"));
        mockMvc.perform(put(URL + "/2/like/2")
                .contentType("application/json"));
        mockMvc.perform(put(URL + "/2/like/1")
                .contentType("application/json"));

        film.setId(1);
        newFilm.setId(2);
        film.getLikes().add(1);
        newFilm.getLikes().add(2);
        newFilm.getLikes().add(1);

        // получаем популярные фильмы
        Film[] actualPopularFilms = gson.fromJson(mockMvc.perform(get(URL + "/popular")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString(), Film[].class);
        Film[] expectedPopularFilms = {newFilm, film};

        assertEquals(expectedPopularFilms[0].getId(), actualPopularFilms[0].getId());
        assertEquals(expectedPopularFilms[1].getId(), actualPopularFilms[1].getId());
    }

    @Test
    void shouldGet400WhenPopularFilmsCountIs0() throws Exception {
        mockMvc.perform(get(URL + "/popular?count=0")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGet400WhenPopularFilmsCountIsNegative() throws Exception {
        mockMvc.perform(get(URL + "/popular?count=-1")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}