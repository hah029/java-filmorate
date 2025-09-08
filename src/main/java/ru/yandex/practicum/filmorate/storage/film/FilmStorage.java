package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.models.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> list();

    Film get(long filmId);

    Film create(Film film);

    Film update(Film film);

    boolean notExists(long filmId);
}
