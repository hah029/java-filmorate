package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class GenreService {
    public final GenreStorage storage;

    @Autowired
    public GenreService(@Qualifier("genreDbStorage") GenreStorage storage) {
        this.storage = storage;
    }

    public Collection<Genre> getList() {
        return storage.list();
    }

    public Optional<Genre> get(long id) {
        if (storage.notExists(id)) {
            log.error("Жанр с id = {} не найден.", id);
            throw new NotFoundException(String.format("Жанр с id = %d не найден.", id));
        }

        return Optional.ofNullable(storage.get(id));
    }
}
