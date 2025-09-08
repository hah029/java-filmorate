package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.models.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> list();

    User get(long userId);

    User create(User user);

    User update(User user);

    boolean notExists(long userId);
}
