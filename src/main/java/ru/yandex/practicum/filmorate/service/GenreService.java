package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    public final GenreStorage storage;

    public Collection<Genre> getList() {
        return storage.list();
    }

    public Genre get(long id) {
        if (storage.notExists(id)) {
            log.error("Жанр с id = {} не найден.", id);
            throw new NotFoundException(String.format("Жанр с id = %d не найден.", id));
        }

        return storage.get(id);
    }
}
