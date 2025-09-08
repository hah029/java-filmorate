package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.models.Genre;

import java.util.Collection;

public interface GenreStorage {
    Collection<Genre> list();

    Genre get(long id);

    boolean notExists(long id);
}
