package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class UserService {
    public final UserStorage storage;

    public Collection<User> getList() {
        return storage.list();
    }

    public User get(long userId) {
        if (storage.notExists(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        return storage.get(userId);
    }

    public User add(User newUser) {
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
            log.trace("Логин ({}) использован в качестве имени пользователя", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        LocalDate today = LocalDate.now();
        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(today)) {
            log.error("Ошибка добавления пользователя: день рождения не может быть больше {}", today);
            throw new ValidationException("День рождения не может быть больше " + today);
        }

        return storage.create(newUser);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.error("Ошибка обновления пользователя: id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
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
            log.trace("Логин ({}) использован в качестве имени пользователя", user.getLogin());
            user.setName(user.getLogin());
        }

        LocalDate today = LocalDate.now();
        if (user.getBirthday() != null && user.getBirthday().isAfter(today)) {
            log.error("Ошибка создания пользователя: день рождения не может быть больше {}", today);
            throw new ValidationException("День рождения не может быть больше " + today);
        }

        return storage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        if (storage.notExists(userId)) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(friendId)) {
            log.error("Друг с id: {} не найден.", friendId);
            throw new NotFoundException("Пользователь с id: " + friendId + "не найден.");
        }

        User user = storage.get(userId);

        if (user.getFriends().containsKey(friendId)) {
            log.error("Пользователь {} уже находится в списке друзей {}", friendId, userId);
            throw new ValidationException(
                    String.format("Пользователь %d уже находится в списке друзей %d", friendId, userId)
            );
        }

        user.getFriends().put(friendId, false);
        storage.update(user);
    }

    public void removeFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Пользователь не может добавить самого себя в друзья");
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }
        if (storage.notExists(userId)) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(friendId)) {
            log.error("Друг с id: {} не найден.", friendId);
            throw new NotFoundException("Друг с id: " + friendId + "не найден.");
        }

        User user = storage.get(userId);

        if (!user.getFriends().containsKey(friendId)) {
            log.trace("Не получилось удалить друга {} из списка друзей пользователя {}. Друг не найден.", friendId, userId);
            return;
        }

        user.getFriends().remove(friendId);
        storage.update(user);
    }

    public Collection<User> getFriends(long userId) {
        if (storage.notExists(userId)) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        User user = storage.get(userId);
        return user.getFriends().keySet().stream().map(storage::get).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        if (storage.notExists(userId)) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        if (storage.notExists(otherId)) {
            log.error("Пользователь с id: {} не найден.", otherId);
            throw new NotFoundException("Пользователь с id: " + otherId + " не найден.");
        }

        User user = storage.get(userId);
        User other = storage.get(otherId);

        Set<Long> userFriends = user.getFriends().keySet();
        Set<Long> otherFriends = other.getFriends().keySet();

        Set<Long> commonFriends = new HashSet<>(userFriends);
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
