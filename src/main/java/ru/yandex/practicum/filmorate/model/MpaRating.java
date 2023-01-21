package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class MpaRating {
    private int id;
    private String name;
}
