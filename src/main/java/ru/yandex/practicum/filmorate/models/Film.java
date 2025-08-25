package ru.yandex.practicum.filmorate.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.yandex.practicum.filmorate.serializers.DurationToMinutesSerializer;
import ru.yandex.practicum.filmorate.serializers.MinutesToDurationDeserializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Set<Integer> likes = new HashSet<>();

    @JsonSerialize(using = DurationToMinutesSerializer.class)
    @JsonDeserialize(using = MinutesToDurationDeserializer.class)
    private Duration duration;
}
