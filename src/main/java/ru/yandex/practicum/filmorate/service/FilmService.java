package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    public final FilmStorage storage;
    public final UserStorage userStorage;
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final LocalDate START_FILM_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    @Autowired
    public FilmService(FilmStorage storage, UserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getList() {
        return storage.list();
    }

    public Optional<Film> get(int filmId) {
        return Optional.ofNullable(storage.get(filmId));
    }

    public Film add(Film newFilm) {
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

        Film addedFilm = storage.create(newFilm);

        log.info("Добавлен новый фильм с id={}", addedFilm.getId());

        return newFilm;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.error("Ошибка обновления фильма: id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        log.info("Обновление фильма с id={}", film.getId());

        if (storage.notExists(film.getId())) {
            log.error("Ошибка обновления фильма: фильм с указанным id={} не найден", film.getId());
            throw new NotFoundException("Фильм с указанным Id не найден");
        }

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

        Film updatedFilm = storage.update(film);
        log.info("Фильм с id={} успешно обновлен", film.getId());

        return updatedFilm;
    }

    public void addLike(int filmId, int userId) {
        if (storage.notExists(filmId)) {
            throw new NotFoundException("Фильм с id: " + filmId + " не найден.");
        }
        if (userStorage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + "не найден.");
        }

        Film film = storage.get(filmId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException(
                    String.format("Пользователь %d уже поставил лайк фильму %d", userId, filmId)
            );
        }

        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        if (storage.notExists(filmId)) {
            throw new NotFoundException("Фильм с id: " + filmId + " не найден.");
        }
        if (userStorage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + "не найден.");
        }

        Film film = storage.get(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException(
                    String.format("Пользователь %d не ставил лайк фильму %d", userId, filmId)
            );
        }

        film.getLikes().remove(userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество count должен быть положительным числом.");
        }

        return storage.list().stream()
                .sorted(
                        Comparator.comparingInt(
                                (Film obj) -> Optional.ofNullable(obj.getLikes())
                                        .map(Set::size)
                                        .orElse(0)
                        ).reversed()
                ).limit(count)
                .collect(Collectors.toList());
    }
}
