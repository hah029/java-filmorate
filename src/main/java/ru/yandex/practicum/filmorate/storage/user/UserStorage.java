package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.models.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> list();

    public User get(int userId);

    public User create(User user);

    public User update(User user);

    public boolean notExists(int userId);
}
