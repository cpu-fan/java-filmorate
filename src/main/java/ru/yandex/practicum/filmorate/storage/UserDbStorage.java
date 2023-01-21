package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("userStorageDaoImpl")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Collection<User> getUsers() {
        String sql = "select * from users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowUser);
        setFriends(users);
        return users;
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        int userId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue();
        return user.toBuilder().id(userId).build();
    }

    @Override
    public User updateUser(User user) {
        String sql = "update users set login = ?, name = ?, email = ?, birthday = ? where id = ?";
        int userId = user.getId();
        jdbcTemplate.update(sql,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                userId);
        return getUserById(userId);
    }

    @Override
    public User getUserById(int id) {
        String sql = "select * from users where id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowUser, id);
        if (users.isEmpty()) {
            log.error(String.format("Пользователь с id = %d не найден", id));
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", id));
        }
        setFriends(users);
        return users.get(0);
    }

    @Override
    public User addFriend(int userId, int friendId) {
        String sqlCheck = "select * from friends where user_id = ? and friend_id = ?";
        List<int[][]> isExist = jdbcTemplate.query(sqlCheck, this::mapRowFriends, userId, friendId);
        if (isExist.size() > 0) {
            String errorMessage = String.format("Пользователь id = %d уже отправлял заявку пользователю id = %d",
                    userId, friendId);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        String sqlInsert = "insert into friends values (?, ?)";
        jdbcTemplate.update(sqlInsert, userId, friendId);
        return getUserById(userId);
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        String sql = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        return getUserById(userId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "select f.friend_id as id, u.login, u.name, u.email, u.birthday " +
                "from friends as f " +
                "inner join users as u on f.friend_id = u.id where f.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, this::mapRowUser, userId);
        setFriends(friends);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "select f1.friend_id as id, u.login, u.name, u.email, u.birthday " +
                "from friends as f1 " +
                "inner join friends as f2 on f1.friend_id = f2.friend_id " +
                "inner join users as u on f1.friend_id = u.id " +
                "where f1.user_id = ? and f2.user_id = ?";
        List<User> commonFriends = jdbcTemplate.query(sql, this::mapRowUser, userId, otherId);
        setFriends(commonFriends);
        return commonFriends;
    }

    private User mapRowUser(ResultSet rs, int rowNum) throws SQLException {
        int userId = rs.getInt("id");

        return User.builder()
                .id(userId)
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    private void setFriends(List<User> users) {
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        SqlParameterSource userIdsSqlParam = new MapSqlParameterSource("userIds", userMap.keySet());
        String sqlFilmLikes = "select * from friends where user_id in (:userIds)";
        namedJdbcTemplate.query(sqlFilmLikes, userIdsSqlParam, rs -> {
            userMap.get(rs.getInt("user_id")).getFriends().add(rs.getInt("friend_id"));
        });
    }

    private int[][] mapRowFriends(ResultSet rs, int rowNum) throws SQLException {
        return new int[][]{{rs.getInt("user_id"), rs.getInt("friend_id")}};
    }
}
