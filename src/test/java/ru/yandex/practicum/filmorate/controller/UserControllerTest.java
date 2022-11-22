package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private User user;
    private static HttpClient client;
    private static HttpRequest request;
    private static HttpResponse<String> response;
    private static Gson gson;
    private static final String URL = "http://localhost:8080/users";

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUpBeforeAll() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @BeforeEach
    void setUp() {
        user = User.builder()
                .login("McLovin")
                .name("Fogell")
                .email("mclovin@my-email.com")
                .birthday(LocalDate.parse("1992-06-20", DateTimeFormatter.ISO_DATE))
                .build();
    }

    @Test
    void testTestUser() throws Exception {
//        mvc.perform(get("http://localhost:8080/users")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mvc.perform(post("http://localhost:8080/users")
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk());
    }

//    @Test
//    void shouldCreateUser() {
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(200, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserEmailIsEmpty() {
//        user = user.toBuilder()
//                .email("")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserEmailDontContainAtSing() {
//        user = user.toBuilder()
//                .email("mclovinmy-email.com")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserLoginIsEmpty() {
//        user = user.toBuilder()
//                .login("")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserLoginIsContainsSpaceAtBeginning() {
//        user = user.toBuilder()
//                .login(" mclovin")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserLoginIsContainsSpaceAtEnd() {
//        user = user.toBuilder()
//                .login("mclovin ")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserLoginIsContainsSpaceInMiddle() {
//        user = user.toBuilder()
//                .login("mc lovin")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn200WhenUserNameIsEmptyAndLoginWillBeUsedAsName() {
//        user = user.toBuilder()
//                .login("McLovin")
//                .name("")
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//        String actual = gson.fromJson(response.body(), User.class).getName();
//
//        assertEquals(200, response.statusCode());
//        assertEquals("McLovin", actual);
//    }
//
//    @Test
//    void shouldReturn200WhenUserBirthdayToday() {
//        user = user.toBuilder()
//                .birthday(LocalDate.now())
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(200, response.statusCode());
//    }
//
//    @Test
//    void shouldReturn400WhenUserBirthdayInFuture() {
//        user = user.toBuilder()
//                .birthday(LocalDate.now().plusDays(1))
//                .build();
//        response = getResponse("POST", gson.toJson(user));
//
//        assertEquals(400, response.statusCode());
//    }
//
//    @Test
//    void shouldUpdatedFilms() {
//        user = user.toBuilder()
//                .id(user.getId() + 1)
//                .login("updatedlogin")
//                .name("updated name")
//                .email("updated@email.com")
//                .birthday(LocalDate.parse("2001-11-11", DateTimeFormatter.ISO_DATE))
//                .build();
//        String expected = gson.toJson(user);
//        response = getResponse("PUT", gson.toJson(user));
//        String actual = response.body();
//
//        assertEquals(200, response.statusCode());
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    void shouldGetAllUsers() {
//        response = getResponse("GET", "");
//        assertEquals(200, response.statusCode());
//    }
//
//    // Вот эти методы полностью дублируют методы из FilmControllerTest
//    // (вопрос там, если вдруг отсюда начал проверку :) ).
//    private HttpResponse<String> getResponse(String method, String body) {
//        switch (method) {
//            case "GET":
//                response = responseForGETMethod();
//                break;
//            case "POST":
//                response = responseForPOSTMethod(body);
//                break;
//            case "PUT":
//                response = responseForPUTMethod(body);
//                break;
//        }
//        return response;
//    }
//
//    private HttpResponse<String> responseForGETMethod() {
//        client = HttpClient.newHttpClient();
//        request = HttpRequest.newBuilder()
//                .uri(URI.create(URL))
//                .GET()
//                .build();
//        try {
//            return client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private HttpResponse<String> responseForPOSTMethod(String body) {
//        client = HttpClient.newHttpClient();
//        request = HttpRequest.newBuilder()
//                .uri(URI.create(URL))
//                .header("Content-type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(body))
//                .build();
//        try {
//            return client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

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