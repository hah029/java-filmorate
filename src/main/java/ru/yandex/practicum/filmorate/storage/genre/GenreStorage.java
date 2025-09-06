package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.models.Genre;

import java.util.Collection;

public interface GenreStorage {
    public Collection<Genre> list();

    public Genre get(long id);

    public boolean notExists(long id);
}
