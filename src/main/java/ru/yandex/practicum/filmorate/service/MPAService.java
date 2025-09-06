package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.models.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class MPAService {
    public final MPAStorage storage;

    @Autowired
    public MPAService(@Qualifier("mpaDbStorage") MPAStorage storage) {
        this.storage = storage;
    }

    public Collection<MPA> getList() {
        return storage.list();
    }

    public Optional<MPA> get(long id) {
        if (storage.notExists(id)) {
            log.error("Рейтинг с id = {} не найден.", id);
            throw new NotFoundException(String.format("Рейтинг с id = %d не найден.", id));
        }
        return Optional.ofNullable(storage.get(id));
    }
}
