package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Getter
@Setter
public class Film {

    private int id;

    @NotBlank
    private String name;

    @Size(min = 0, max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    private Set<Integer> likes;

    public Set<Integer> getLikes() {
        if (likes == null) {
            setLikes();
        }
        return likes;
    }

    private void setLikes() {
        this.likes = new HashSet<>();
    }

    public void addLike(int userId) {
        likes.add(userId);
    }

    public void removeLike(int userId) {
        likes.remove(userId);
    }
}
