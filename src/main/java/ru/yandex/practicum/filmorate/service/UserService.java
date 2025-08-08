package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    public final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> getList() {
        return storage.list();
    }

    public Optional<User> get(int userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    public User add(User newUser) {
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

        User addedUser = storage.create(newUser);

        log.info("Добавлен новый пользователь с id={}", addedUser.getId());
        return addedUser;
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.error("Ошибка обновления пользователя: id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        log.info("Обновление пользователя с id={}", user.getId());

        if (storage.notExists(user.getId())) {
            log.error("Ошибка обновления пользователя: пользователь с указанным id={} не найден", user.getId());
            throw new NotFoundException(String.format("Пользователь с указанным id=%d не найден", user.getId()));
        }

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

        User updatedUser = storage.update(user);
        log.info("Пользователь с id={} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    public void addFriend(int userId, int friendId) {
        if (storage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(friendId)) {
            throw new NotFoundException("Пользователь с id: " + friendId + "не найден.");
        }

        User user = storage.get(userId);
        User friend = storage.get(friendId);

        if (user.getFriends().contains(friendId) || friend.getFriends().contains(userId)) {
            throw new ValidationException(
                    String.format("Пользователи %d и %d уже являются друзьями", userId, friendId)
            );
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        if (storage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(friendId)) {
            throw new NotFoundException("Пользователь с id: " + friendId + "не найден.");
        }

        User user = storage.get(userId);
        User friend = storage.get(friendId);

        if (!user.getFriends().contains(friendId) || !friend.getFriends().contains(userId)) {
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(int userId) {
        if (storage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        User user = storage.get(userId);
        return user.getFriends().stream().map(storage::get).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        if (storage.notExists(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(otherId)) {
            throw new NotFoundException("Пользователь с id: " + otherId + " не найден.");
        }

        User user = storage.get(userId);
        User other = storage.get(otherId);

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherFriends = other.getFriends();

        Set<Integer> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherFriends);

        return commonFriends.stream().map(storage::get).collect(Collectors.toList());

    }

    public static boolean containsWhitespace(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
