package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.controller.utils.testadapters.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan("ru.yandex.practicum.filmorate")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    private User user;
    private User friend;
    private User commonFriend;
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
    void setUp() throws Exception {
        user = User.builder()
                .login("McLovin")
                .name("Fogell")
                .email("mclovin@my-email.com")
                .birthday(LocalDate.parse("1992-06-20", DateTimeFormatter.ISO_DATE))
                .build();
        friend = User.builder()
                .login("userFriend")
                .name("friend")
                .email("friend@my-email.com")
                .birthday(LocalDate.parse("1992-03-10", DateTimeFormatter.ISO_DATE))
                .build();
        commonFriend = User.builder()
                .login("commonFriend")
                .name("common")
                .email("common@my-email.com")
                .birthday(LocalDate.parse("1993-01-25", DateTimeFormatter.ISO_DATE))
                .build();

        String userStr = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String friendStr = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(friend)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String commonStr = mockMvc.perform(post(URL)
                .contentType("application/json")
                .content(gson.toJson(commonFriend)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        user = gson.fromJson(userStr, User.class);
        friend = gson.fromJson(friendStr, User.class);
        commonFriend = gson.fromJson(commonStr, User.class);
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

        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

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

    @Test
    void shouldGet200WhenSendUserId1() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(URL + "/1")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();
        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

        assertEquals(user.getId(), actualUser.getId());
    }

    @Test
    void shouldGet400WhenSendUserIdMinus1() throws Exception {
        mockMvc.perform(get(URL + "/-1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddFriend() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();
        user.getFriends().add(2);
        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

        assertEquals(user.getFriends(), actualUser.getFriends());
    }

    @Test
    void shouldAutoMutuallyAddedToFriends() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        User actualFriend = gson.fromJson(mockMvc.perform(get(URL + "/2"))
                .andReturn()
                .getResponse()
                .getContentAsString(), User.class);

        assertEquals(user.getId(), actualFriend.getFriends().toArray()[0]);
    }

    @Test
    void shouldGet404WhenAddFriendAndUserNotFound() throws Exception {
        mockMvc.perform(put(URL + "/-1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet404WhenAddFriendAndFriendNotFound() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/-2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet400WhenAddFriendAndUserIdEqualsFriendId() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/1")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGet400WhenAddFriendAndThisFriendContainsInFriendList() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteFriend() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        MvcResult mvcResult = mockMvc.perform(delete(URL + "/1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();
        User actualUser = gson.fromJson(mvcResult.getResponse().getContentAsString(), User.class);

        assertEquals(user.getFriends(), actualUser.getFriends());
    }

    @Test
    void shouldAutoMutuallyDeletedToFriends() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        mockMvc.perform(delete(URL + "/1/friends/2")
                .contentType("application/json"));
        User actualUser = gson.fromJson(mockMvc.perform(get(URL + "/1"))
                .andReturn()
                .getResponse()
                .getContentAsString(), User.class);
        User actualFriend = gson.fromJson(mockMvc.perform(get(URL + "/2"))
                .andReturn()
                .getResponse()
                .getContentAsString(), User.class);

        assertEquals(actualUser.getFriends().isEmpty(), actualFriend.getFriends().isEmpty());
    }

    @Test
    void shouldGet404WhenDeleteFriendAndUserNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/-1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet404WhenDeleteFriendAndFriendNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/1/friends/-2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet400WhenDeleteFriendAndUserIdEqualsFriendId() throws Exception {
        mockMvc.perform(delete(URL + "/1/friends/1")
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGet404WhenDeleteFriendAndThisFriendNotContainsInFriendList() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        mockMvc.perform(delete(URL + "/1/friends/2")
                .contentType("application/json"));
        mockMvc.perform(delete(URL + "/1/friends/2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetFriends() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/2")
                .contentType("application/json"));
        mockMvc.perform(put(URL + "/1/friends/3")
                .contentType("application/json"));
        String actualListFriends = mockMvc.perform(get(URL + "/1/friends")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        friend.getFriends().add(1);
        commonFriend.getFriends().add(1);
        String expectedListFriends = gson.toJson(List.of(friend, commonFriend));

        assertEquals(expectedListFriends, actualListFriends);
    }

    @Test
    void shouldGet404WhenGetFriendsAndUserNotFound() throws Exception {
        mockMvc.perform(get(URL + "/-1/friends")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        mockMvc.perform(put(URL + "/1/friends/3")
                .contentType("application/json"));
        mockMvc.perform(put(URL + "/2/friends/3")
                .contentType("application/json"));
        String actualCommonFriends = mockMvc.perform(get(URL + "/1/friends/common/2")
                .contentType("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        commonFriend.getFriends().add(1);
        commonFriend.getFriends().add(2);
        String expectedCommonFriends = gson.toJson(List.of(commonFriend));

        assertEquals(expectedCommonFriends, actualCommonFriends);
    }

    @Test
    void shouldGet400WhenGetCommonFriendsAndUserNotFound() throws Exception {
        mockMvc.perform(get(URL + "/-1/friends/common/2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGet400WhenGetCommonFriendsAndFriendNotFound() throws Exception {
        mockMvc.perform(get(URL + "/1/friends/common/-2")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }
}