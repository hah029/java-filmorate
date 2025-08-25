package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Film;

import java.util.Collection;
import java.util.HashMap;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Integer, Film> films = new HashMap<>();
    private static int currentMaxId = 0;

    private int generateId() {
        return ++currentMaxId;
    }

    @Override
    public Film create(Film newFilm) {
        newFilm.setId(generateId());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film get(int filmId) {
        return films.get(filmId);
    }

    @Override
    public boolean notExists(int filmId) {
        return !films.containsKey(filmId);
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = get(film.getId());

        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());
        oldFilm.setLikes(film.getLikes());

        return oldFilm;
    }

    @Override
    public Collection<Film> list() {
        return films.values();
    }

}
