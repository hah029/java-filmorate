package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final HashMap<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUserList() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User newUser) {

        log.info("Добавление пользователя {}", newUser);

        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            log.error("Ошибка добавления пользователя: электронная почта не может быть пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!newUser.getEmail().contains("@")) {
            log.error("Ошибка добавления пользователя: электронная почта должна содержать символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (newUser.getLogin() == null || newUser.getLogin().isBlank()) {
            log.error("Ошибка добавления пользователя: логин не может быть пустым");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (containsWhitespace(newUser.getLogin())) {
            log.error("Ошибка добавления пользователя: логин не должен содержать пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Логин ({}) использован в качестве имени пользователя", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        LocalDate today = LocalDate.now();
        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(today)) {
            log.error("Ошибка добавления пользователя: день рождения не может быть больше {}", today);
            throw new ValidationException("День рождения не может быть больше " + today);
        }

        newUser.setId(generateId());
        users.put(newUser.getId(), newUser);

        log.info("Добавлен новый пользователь с id={}", newUser.getId());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {

        if (user.getId() == null) {
            log.error("Ошибка обновления пользователя: id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        log.info("Обновление пользователя с id={}", user.getId());

        if (users.containsKey(user.getId())) {

            User oldUser = users.get(user.getId());

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                log.error("Ошибка создания пользователя: электронная почта не может быть пустой");
                throw new ValidationException("Электронная почта не может быть пустой");
            }
            if (!user.getEmail().contains("@")) {
                log.error("Ошибка создания пользователя: электронная почта должна содержать символ @");
                throw new ValidationException("Электронная почта должна содержать символ @");
            }

            if (user.getLogin() == null || user.getLogin().isBlank()) {
                log.error("Ошибка создания пользователя: логин не может быть пустым");
                throw new ValidationException("Логин не может быть пустым");
            }
            if (containsWhitespace(user.getLogin())) {
                log.error("Ошибка создания пользователя: логин не должен содержать пробелы");
                throw new ValidationException("Логин не должен содержать пробелы");
            }

            if (user.getName() == null || user.getName().isBlank()) {
                log.info("Логин ({}) использован в качестве имени пользователя", user.getLogin());
                user.setName(user.getLogin());
            }

            LocalDate today = LocalDate.now();
            if (user.getBirthday() != null && user.getBirthday().isAfter(today)) {
                log.error("Ошибка создания пользователя: день рождения не может быть больше {}", today);
                throw new ValidationException("День рождения не может быть больше " + today);
            }

            oldUser.setLogin(user.getLogin());
            oldUser.setEmail(user.getEmail());
            oldUser.setName(user.getName());
            oldUser.setBirthday(user.getBirthday());

            log.info("Пользователь с id={} успешно обновлен", user.getId());
            return oldUser;
        }

        log.error("Ошибка обновления пользователя: пользователь с указанным id={} не найден", user.getId());
        throw new NotFoundException(String.format("Пользователь с указанным id=%d не найден", user.getId()));
    }

    private int generateId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean containsWhitespace(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

}
