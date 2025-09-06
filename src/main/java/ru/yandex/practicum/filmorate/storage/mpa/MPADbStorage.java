package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.models.MPA;

import java.util.Collection;


@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MPADbStorage implements MPAStorage {
    private final JdbcTemplate jdbc;

    public Collection<MPA> list() {
        String sql = "SELECT * FROM mpa";
        return jdbc.query(sql, (rs, rowNum) -> {
            MPA mpa = new MPA();
            mpa.setId(rs.getLong("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    public MPA get(long id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            MPA mpa = new MPA();
            mpa.setId(rs.getLong("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }, id);
    }

    @Override
    public boolean notExists(long id) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count == null || count == 0;
    }
}
