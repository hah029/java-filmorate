package ru.yandex.practicum.filmorate.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

@TestConfiguration
public class TestConfig {

    @Bean
    public UserRowMapper userRowMapper() {
        return new UserRowMapper();
    }

    @Bean
    public FilmRowMapper filmRowMapper() {
        return new FilmRowMapper();
    }
}