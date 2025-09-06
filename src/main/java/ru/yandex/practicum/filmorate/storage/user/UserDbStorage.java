package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.models.User;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    public Collection<User> list() {
        String query = "SELECT * FROM users";
        return jdbc.query(query, mapper);
    }

    public User get(long id) {
        String query = "SELECT * FROM users WHERE id = ? LIMIT 1";
        User user = jdbc.queryForObject(query, mapper, id);
        if (user != null) {
            loadFriends(user);
        }
        return user;
    }

    public User create(User user) {
        String query = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getBirthday().toString());
            return stmt;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
        return user;
    }

    public User update(User user) {
        String query = "UPDATE users SET " +
                "email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE id = ?";

        jdbc.update(query
            , user.getEmail()
            , user.getLogin()
            , user.getName()
            , user.getBirthday().toString()
            , user.getId());

        saveFriends(user);
        return user;
    }

    @Override
    public boolean notExists(long filmId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId);
        return count == null || count == 0;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id, status FROM friends WHERE user_id = ?";
        jdbc.query(sql, (rs) -> {
            user.getFriends().put(rs.getLong("friend_id"), rs.getBoolean("status"));
        }, user.getId());
    }

    private void saveFriends(User user) {
        String deleteSql = "DELETE FROM friends WHERE user_id = ?";
        jdbc.update(deleteSql, user.getId());

        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        user.getFriends().forEach((friendId, status) -> {
            jdbc.update(insertSql, user.getId(), friendId, status);
        });
    }
}