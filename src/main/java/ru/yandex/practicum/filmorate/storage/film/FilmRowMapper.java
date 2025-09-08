package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getObject("release_date", LocalDate.class));

        long mpaId = resultSet.getLong("mpa_id");
        MPA mpa = new MPA();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        long durationMinutes = resultSet.getLong("duration");
        film.setDuration(Duration.ofMinutes(durationMinutes));

        return film;
    }
}
