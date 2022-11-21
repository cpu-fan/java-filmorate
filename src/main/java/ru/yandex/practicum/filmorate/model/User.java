package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
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
}
