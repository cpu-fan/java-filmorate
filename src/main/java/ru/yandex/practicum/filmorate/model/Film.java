package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class Film {

    private int id;

    @NotBlank
    private String name;

    @Size(min = 0, max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;
}
