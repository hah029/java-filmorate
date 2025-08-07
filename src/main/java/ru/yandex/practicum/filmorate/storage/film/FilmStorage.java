package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.models.Film;

import java.util.Collection;

public interface FilmStorage {
    public Collection<Film> list();

    public Film get(int filmId);

    public Film create(Film film);

    public Film update(Film film);

    public boolean notExists(int filmId);
}
