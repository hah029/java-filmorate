package ru.yandex.practicum.filmorate.controller;

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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createUser_WithValidData_ShouldReturn200() throws Exception {
        // Given
        String userJson = objectMapper.writeValueAsString(validUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Электронная почта не может быть пустой", result.getResolvedException().getMessage());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Электронная почта должна содержать символ @", result.getResolvedException().getMessage());
    }

    @Test
    void createUser_WithEmptyLogin_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Логин не может быть пустым", result.getResolvedException().getMessage());
    }

    @Test
    void createUser_WithLoginContainingWhitespace_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login with space");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Логин не должен содержать пробелы", result.getResolvedException().getMessage());
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("");
        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin"));
    }

    @Test
    void createUser_WithFutureBirthday_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setBirthday(LocalDate.now().plusDays(1));
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertTrue(result.getResolvedException().getMessage().contains("День рождения не может быть больше"));
    }

    @Test
    void updateUser_WithValidData_ShouldReturn200() throws Exception {
        // Given: Создаём пользователя
        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andReturn();
        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                User.class
        );
        createdUser.setName("Updated Name");
        String updatedJson = objectMapper.writeValueAsString(createdUser);

        // When & Then: Обновляем
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_WithoutId_ShouldReturn400() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertInstanceOf(ValidationException.class, result.getResolvedException());
        assertEquals("Id должен быть указан", result.getResolvedException().getMessage());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        User user = new User();
        user.setId(9999);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        String userJson = objectMapper.writeValueAsString(user);

        // When
        MvcResult result = mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        assertInstanceOf(NotFoundException.class, result.getResolvedException());
        assertEquals("Пользователь с указанным id=9999 не найден", result.getResolvedException().getMessage());
    }
}