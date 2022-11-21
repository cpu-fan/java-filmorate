package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmControllerTest {
    private Film film;
    private static HttpClient client;
    private static HttpRequest request;
    private static HttpResponse<String> response;
    private static Gson gson;
    private static final String URL = "http://localhost:8080/films";

    @BeforeAll
    static void setUpBeforeAll() {
        FilmorateApplication.main(new String[] {});
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @BeforeEach
    void setUp() {
        film = Film.builder()
                .name("District 9")
                .description("My favorite movie")
                .releaseDate(LocalDate.parse("2009-08-13", DateTimeFormatter.ISO_DATE))
                .duration(112)
                .build();
    }

    @Test
    void shouldCreateFilm() {
        response = getResponse("POST", gson.toJson(film));
        int idForExpected = gson.fromJson(response.body(), Film.class).getId();
        String expected = gson.toJson(film.toBuilder().id(idForExpected).build());

        assertEquals(200, response.statusCode());
        assertEquals(expected, response.body());
    }

    @Test
    void shouldReturn400WhenFilmNameEmpty() {
        film = film.toBuilder()
                .name("")
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturn200WhenFilmDescriptionLengthIs200Symbols() {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, нуу")
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturn200WhenFilmDescriptionLengthIs199Symbols() {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, ну")
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturn400WhenFilmDescriptionLengthIs201Symbols() {
        film = film.toBuilder()
                .description("У этого фильма очень, очень, ну очень длинное описание. У этого фильма " +
                "очень, очень, ну очень длинное описание. У этого фильма очень, очень, ну очень длинное описание. " +
                "У этого фильма очень, очень, нууу")
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturn200WhenFilmDateIs28Dec1895() {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_DATE))
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturn200WhenFilmDateAfter28Dec1895() {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-29", DateTimeFormatter.ISO_DATE))
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturn400WhenFilmDateBefore28Dec1895() {
        film = film.toBuilder()
                .releaseDate(LocalDate.parse("1895-12-27", DateTimeFormatter.ISO_DATE))
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturn200WhenFilmDurationIsPositive() {
        film = film.toBuilder()
                .duration(1)
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldReturn400WhenFilmDurationIs0() {
        film = film.toBuilder()
                .duration(0)
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturn400WhenFilmDurationIsNegative() {
        film = film.toBuilder()
                .duration(-1)
                .build();
        response = getResponse("POST", gson.toJson(film));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldUpdatedFilms() {
        film = film.toBuilder()
                .id(film.getId() + 1)
                .name("updated name")
                .description("updated desc")
                .releaseDate(LocalDate.parse("2001-11-11", DateTimeFormatter.ISO_DATE))
                .duration(111)
                .build();
        String expected = gson.toJson(film);
        response = getResponse("PUT", gson.toJson(film));
        String actual = response.body();

        assertEquals(200, response.statusCode());
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetFilms() {
        response = getResponse("GET", "");
        assertEquals(200, response.statusCode());
    }

    // Ниже идут методы, которые отправляют запросы на эндпоинты. Клиент реализовал стандартными средствами Java,
    // т.к. через SpringBoot было дольше разбираться и пока что не понятней. А тот способ, который предлагал реализовать
    // в тестах наставник - показался еще более запутанным для меня на данный момент. Тем более в ТЗ явно не просили
    // реализовать тесты с помощью возможностей SpringBoot, поэтому сделал пока так. Всему свое время :)
    //
    // Но есть проблема в дублировании: ниже приведенные методы, абсолютно такие же, которые есть и в UserControllerTest.
    // Как будет правильнее, сделать отдельный пакет с классом/ми для этих методов внутри пакета test/.../controller
    // или сделать эти методы и класс LocalDateAdapter статическими и использовать в UserControllerTest?
    // Или ни то и ни другое?
    private HttpResponse<String> getResponse(String method, String body) {
        switch (method) {
            case "GET":
                response = responseForGETMethod();
                break;
            case "POST":
                response = responseForPOSTMethod(body);
                break;
            case "PUT":
                response = responseForPUTMethod(body);
                break;
        }
        return response;
    }

    private HttpResponse<String> responseForGETMethod() {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> responseForPOSTMethod(String body) {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> responseForPUTMethod(String body) {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {

        @Override
        public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
            if (localDate != null) {
                jsonWriter.value(localDate.format(DateTimeFormatter.ISO_DATE));
            } else {
                jsonWriter.nullValue();
            }
        }

        @Override
        public LocalDate read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            } else {
                return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_DATE);
            }
        }
    }
}