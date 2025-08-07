package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.User;

import java.util.Collection;
import java.util.HashMap;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public User create(User newUser) {
        newUser.setId(generateId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User get(int userId) {
        return users.get(userId);
    }

    @Override
    public boolean notExists(int userId) {
        return !users.containsKey(userId);
    }

    @Override
    public User update(User user) {
        User oldUser = get(user.getId());

        oldUser.setLogin(user.getLogin());
        oldUser.setEmail(user.getEmail());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());
        oldUser.setFriends(user.getFriends());

        return oldUser;
    }

    @Override
    public Collection<User> list() {
        return users.values();
    }

    private int generateId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
