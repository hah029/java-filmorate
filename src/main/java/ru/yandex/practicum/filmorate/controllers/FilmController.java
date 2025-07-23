package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final HashMap<Integer, Film> films = new HashMap<>();
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final LocalDate START_FILM_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    @GetMapping
    public Collection<Film> getFilmList() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film newFilm) {

        log.info("Добавление фильма {}", newFilm);

        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.error("Ошибка добавления фильма: название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (newFilm.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Ошибка добавления фильма: описание не может быть длиннее {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ValidationException(String.format("Описание не может быть длиннее %d символов", MAX_DESCRIPTION_LENGTH));
        }
        if (newFilm.getReleaseDate().isBefore(START_FILM_DATE)) {
            log.error("Ошибка добавления фильма: дата релиза не может быть раньше {}", START_FILM_DATE);
            throw new ValidationException(String.format("Дата релиза не может быть раньше %s", START_FILM_DATE));
        }
        if (newFilm.getDuration().isNegative()) {
            log.error("Ошибка добавления фильма: продолжительность фильма не может быть отрицательной");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }

        newFilm.setId(generateId());
        films.put(newFilm.getId(), newFilm);

        log.info("Добавлен новый фильм с id={}", newFilm.getId());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {

        if (film.getId() == null) {
            log.error("Ошибка обновления фильма: id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        log.info("Обновление фильма с id={}", film.getId());

        if (!films.containsKey(film.getId())) {
            log.error("Ошибка обновления фильма: фильм с указанным id={} не найден", film.getId());
            throw new NotFoundException("Фильм с указанным Id не найден");
        }

        Film oldFilm = films.get(film.getId());

        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка обновления фильма: название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Ошибка обновления фильма: описание не может быть длиннее {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ValidationException(String.format("Описание не может быть длиннее %d символов", MAX_DESCRIPTION_LENGTH));
        }

        if (film.getReleaseDate().isBefore(START_FILM_DATE)) {
            log.error("Ошибка обновления фильма: дата релиза не может быть раньше {}", START_FILM_DATE);
            throw new ValidationException(String.format("Дата релиза не может быть раньше %s", START_FILM_DATE));
        }

        if (film.getDuration().isNegative()) {
            log.error("Ошибка обновления фильма: продолжительность фильма не может быть отрицательной");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }

        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        log.info("Фильм с id={} успешно обновлен", film.getId());
        return oldFilm;
    }

    private int generateId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
