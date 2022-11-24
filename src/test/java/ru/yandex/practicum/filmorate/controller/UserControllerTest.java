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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ComponentScan("ru.yandex.practicum.filmorate")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    private User user;
    private static Gson gson;
    private static final String URL = "http://localhost:8080/users";

    @Autowired
    private MockMvc mockMvc;

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
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenUserEmailIsEmpty() throws Exception {
        user = user.toBuilder()
                .email("")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUserEmailDontContainAtSing() throws Exception {
        user = user.toBuilder()
                .email("mclovinmy-email.com")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUserLoginIsEmpty() throws Exception {
        user = user.toBuilder()
                .login("")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUserLoginIsContainsSpaceAtBeginning() throws Exception {
        user = user.toBuilder()
                .login(" mclovin")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUserLoginIsContainsSpaceAtEnd() throws Exception {
        user = user.toBuilder()
                .login("mclovin ")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUserLoginIsContainsSpaceInMiddle() throws Exception {
        user = user.toBuilder()
                .login("mc lovin")
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenUserNameIsEmptyAndLoginWillBeUsedAsName() throws Exception {
        user = user.toBuilder()
                .login("McLovin")
                .name("")
                .build();

        MvcResult mvcResult = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn();

        int actualStatusCode = mvcResult.getResponse().getStatus();
        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

        assertEquals(200, actualStatusCode);
        assertEquals("McLovin", actualUser.getName());
    }

    @Test
    void shouldReturn200WhenUserBirthdayToday() throws Exception {
        user = user.toBuilder()
                .birthday(LocalDate.now())
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenUserBirthdayInFuture() throws Exception {
        user = user.toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatedFilms() throws Exception {
        user = user.toBuilder()
                .id(user.getId() + 1)
                .login("updatedlogin")
                .name("updated name")
                .email("updated@email.com")
                .birthday(LocalDate.parse("2001-11-11", DateTimeFormatter.ISO_DATE))
                .build();

        MvcResult mvcResult = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn();

        int actualStatusCode = mvcResult.getResponse().getStatus();
        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

        assertEquals(200, actualStatusCode);
        assertEquals(user.getLogin(), actualUser.getLogin());
        assertEquals(user.getName(), actualUser.getName());
        assertEquals(user.getEmail(), actualUser.getEmail());
        assertEquals(user.getBirthday(), actualUser.getBirthday());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get(URL)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }
}