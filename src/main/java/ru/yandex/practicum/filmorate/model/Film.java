package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    private Set<Integer> likes;

    private MpaRating mpa;

    private Set<Genre> genres;

    public Set<Integer> getLikes() {
        if (likes == null) {
            this.likes = new HashSet<>();
        }
        return likes;
    }

    public void addLike(int userId) {
        likes.add(userId);
    }

    public void removeLike(int userId) {
        likes.remove(userId);
    }

    public Set<Genre> getGenres() {
        if (genres == null) {
            this.genres = new HashSet<>();
        }
        return genres;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("mpa_rating_id", mpa.getId());
        return values;
    }
}
