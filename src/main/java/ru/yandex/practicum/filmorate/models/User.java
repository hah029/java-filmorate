package ru.yandex.practicum.filmorate.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
//    private Set<Long> friends = new HashSet<>();
    private Map<Long, Boolean> friends = new HashMap<>();
}
