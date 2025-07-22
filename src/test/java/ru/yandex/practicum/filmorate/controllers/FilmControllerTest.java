package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("This is a valid film description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(Duration.ofMinutes(120));
    }

    @Test
    void createFilm_WithValidData_ShouldReturn200() throws Exception {
        // Given
        String filmJson = objectMapper.writeValueAsString(validFilm);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createFilm_WithEmptyName_ShouldReturn400() throws Exception {
        // Given
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Название не может быть пустым", result.getResolvedException().getMessage());
    }

    @Test
    void createFilm_WithLongDescription_ShouldReturn400() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Описание не может быть длиннее 200 символов", result.getResolvedException().getMessage());
    }

    @Test
    void createFilm_WithEarlyReleaseDate_ShouldReturn400() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Дата релиза не может быть раньше 1895-12-28", result.getResolvedException().getMessage());
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldReturn400() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(-120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Продолжительность фильма не может быть отрицательной", result.getResolvedException().getMessage());
    }

    @Test
    void updateFilm_WithValidData_ShouldReturn200() throws Exception {
        // Given
        MvcResult createResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn();

        Film createdFilm = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                Film.class
        );
        createdFilm.setName("Updated Name");
        String updatedJson = objectMapper.writeValueAsString(createdFilm);

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateFilm_WithoutId_ShouldReturn400() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Id должен быть указан", result.getResolvedException().getMessage());
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        Film film = new Film();
        film.setId(9999);
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        String filmJson = objectMapper.writeValueAsString(film);

        // When
        MvcResult result = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        assertInstanceOf(NotFoundException.class, result.getResolvedException());
        assertEquals("Фильм с указанным Id не найден", result.getResolvedException().getMessage());
    }
}