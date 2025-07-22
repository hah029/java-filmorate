package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */

@Data
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Duration duration;
}
