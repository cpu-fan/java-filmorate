package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.controller.utils.testadapters.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan("ru.yandex.practicum.filmorate")
@WebMvcTest(controllers = FilmController.class)
class FilmControllerTest {
    private Film film;
    private static final String URL = "http://localhost:8080/films";

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
}