package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.models.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MPAService {
    public final MPAStorage storage;

    public Collection<MPA> getList() {
        return storage.list();
    }

    public MPA get(long id) {
        if (storage.notExists(id)) {
            log.error("Рейтинг с id = {} не найден.", id);
            throw new NotFoundException(String.format("Рейтинг с id = %d не найден.", id));
        }
        return storage.get(id);
    }
}
