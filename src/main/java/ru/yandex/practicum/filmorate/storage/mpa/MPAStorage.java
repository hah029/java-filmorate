package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.models.MPA;

import java.util.Collection;

public interface MPAStorage {
    Collection<MPA> list();

    MPA get(long id);

    boolean notExists(long id);
}
