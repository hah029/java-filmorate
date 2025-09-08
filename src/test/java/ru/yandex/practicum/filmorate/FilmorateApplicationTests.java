package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.config.TestConfig;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.MPA;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({TestConfig.class, UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	private User testUser;
	private Film testFilm;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setLogin("testLogin");
		testUser.setName("Test User");
		testUser.setBirthday(LocalDate.of(1990, 1, 1));

		testFilm = new Film();
		testFilm.setName("Test Film");
		testFilm.setDescription("Test Description");
		testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
		testFilm.setDuration(Duration.ofMinutes(120));

		MPA mpa = new MPA();
		mpa.setId(1L);
		testFilm.setMpa(mpa);
		testFilm.setGenres(new HashSet<>());
	}

	@Test
	void testCreateAndFindUserById() {
		User createdUser = userStorage.create(testUser);
		Optional<User> userOptional = Optional.ofNullable(userStorage.get(createdUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user -> {
					assertThat(user).hasFieldOrPropertyWithValue("id", createdUser.getId());
					assertThat(user.getEmail()).isEqualTo("test@example.com");
					assertThat(user.getLogin()).isEqualTo("testLogin");
				});
	}

	@Test
	void testUpdateUser() {
		User createdUser = userStorage.create(testUser);
		createdUser.setEmail("updated@example.com");
		createdUser.setName("Updated Name");

		User updatedUser = userStorage.update(createdUser);

		assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
		assertThat(updatedUser.getName()).isEqualTo("Updated Name");
	}

	@Test
	void testGetAllUsers() {
		userStorage.create(testUser);
		Collection<User> users = userStorage.list();

		assertThat(users).isNotEmpty();
		assertThat(users.size()).isEqualTo(1);
	}

	@Test
	void testCreateAndFindFilmById() {
		Film createdFilm = filmStorage.create(testFilm);
		Optional<Film> filmOptional = Optional.ofNullable(filmStorage.get(createdFilm.getId()));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film -> {
					assertThat(film).hasFieldOrPropertyWithValue("id", createdFilm.getId());
					assertThat(film.getName()).isEqualTo("Test Film");
					assertThat(film.getDescription()).isEqualTo("Test Description");
				});
	}

	@Test
	void testUpdateFilm() {
		Film createdFilm = filmStorage.create(testFilm);
		createdFilm.setName("Updated Film Name");
		createdFilm.setDescription("Updated Description");

		Film updatedFilm = filmStorage.update(createdFilm);

		assertThat(updatedFilm.getName()).isEqualTo("Updated Film Name");
		assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
	}

	@Test
	void testGetAllFilms() {
		filmStorage.create(testFilm);
		Collection<Film> films = filmStorage.list();

		assertThat(films).isNotEmpty();
		assertThat(films.size()).isEqualTo(1);
	}
}