package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.springframework.context.annotation.Bean;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public Set<Integer> getFriends() {
        return friends;
    }

    public void setFriends() {
        this.friends = new HashSet<>();
    }
}
