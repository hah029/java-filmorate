package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.models.MPA;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    public Collection<Film> list() {
        String query = "SELECT * FROM films";
        Collection<Film> films = jdbc.query(query, mapper);

        for (Film film : films) {
            if (film != null) {
                loadLikes(film);
                loadGenres(film);
                loadMpa(film);
            }
        }

        return films;
    }

    public Film get(long id) {
        String query = "SELECT * FROM films WHERE id = ? LIMIT 1";
        Film film = jdbc.queryForObject(query, mapper, id);
        if (film != null) {
            loadLikes(film);
            loadGenres(film);
            loadMpa(film);
        }
        return film;
    }

    public Film create(Film film) {
        String query = "INSERT INTO films (name, description, release_date, mpa_id, duration) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setString(3, film.getReleaseDate().toString());
            stmt.setLong(4, film.getMpa().getId());
            stmt.setLong(5, film.getDuration().toMinutes());
            return stmt;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        insertFilmGenres(film);
        insertFilmLikes(film);
        loadMpa(film);

        return film;
    }

    public Film update(Film film) {
        String query = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, mpa_id = ?, duration = ? " +
                "WHERE id = ?";

        jdbc.update(
            query,
            film.getName(),
            film.getDescription(),
            film.getReleaseDate().toString(),
            film.getMpa().getId(),
            film.getDuration().toMinutes(),
            film.getId()
        );

        // Обновляем данные жанров
        deleteFilmGenres(film.getId());
        insertFilmGenres(film);

        // Обновляем данные лайков
        deleteFilmLikes(film.getId());
        insertFilmLikes(film);

        // Записываем данные рейтинга
        loadMpa(film);

        return film;
    }

    @Override
    public boolean notExists(long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId);
        return count == null || count == 0;
    }

    private void deleteFilmGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(sql, filmId);
    }

    private void deleteFilmLikes(Long filmId) {
        String sql = "DELETE FROM likes WHERE film_id = ?";
        jdbc.update(sql, filmId);
    }

    private void insertFilmGenres(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbc.update(sql, film.getId(), genre.getId());
        }
    }

    private void insertFilmLikes(Film film) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        for (Long userId : film.getLikes()) {
            jdbc.update(sql, film.getId(), userId);
        }
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        film.setLikes(new HashSet<>(
                jdbc.query(sql, (rs, rowNum) -> rs.getLong("user_id"), film.getId())
        ));
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        Set<Genre> genres = new HashSet<>(jdbc.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId()));
        film.setGenres(genres);
    }

    private void loadMpa(Film film) {
        String sql = "SELECT * FROM mpa WHERE id = ? LIMIT 1";

        film.setMpa(jdbc.queryForObject(sql, (rs, rowNum) -> {
            MPA mpa = new MPA();
            mpa.setId(rs.getLong("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }, film.getMpa().getId()));
    }
}
