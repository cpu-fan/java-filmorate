package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Getter
@Setter
public class User {

    private int id;

    @NotBlank
    @Email(message = "Электронная почта не соответствует формату")
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\w*$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Integer> friends;

    public Set<Integer> getFriends() {
        if (friends == null) {
            setFriends();
        }
        return friends;
    }

    private void setFriends() {
        this.friends = new HashSet<>();
    }

    public void addFriend(int friendId) {
        friends.add(friendId);
    }

    public void removeFriend(int friendId) {
        friends.remove(friendId);
    }
}
