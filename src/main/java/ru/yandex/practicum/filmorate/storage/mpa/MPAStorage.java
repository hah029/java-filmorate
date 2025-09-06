package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.models.MPA;

import java.util.Collection;

public interface MPAStorage {
    public Collection<MPA> list();

    public MPA get(long id);

    public boolean notExists(long id);
}
