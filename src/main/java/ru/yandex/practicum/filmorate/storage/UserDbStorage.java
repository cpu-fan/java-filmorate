package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("userStorageDaoImpl")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> getUsers() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, this::mapRowUser);
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
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.format("Пользователь с id = %d не найден", id));
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", id));
        }
    }

    @Override
    public User addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        String sql = "select is_confirmed from friends where user_id = ? and friend_id = ?";
        List<Boolean> isHaveFriendRequest = jdbcTemplate.query(sql, this::mapRowConfirmed, friendId, userId);
        if (isHaveFriendRequest.size() > 0 && !isHaveFriendRequest.get(0)) {
            String sqlForUpdate = "update friends set user_id = ?, friend_id = ?, is_confirmed = ? " +
                    "where user_id = ? and friend_id = ?";
            jdbcTemplate.update(sqlForUpdate,
                    friendId,
                    userId,
                    true,
                    friendId,
                    userId);
            log.info(String.format("Пользователь id = %d подтвердил заявку пользователя id = %d",
                    friendId, userId));
            User user = getUserById(userId);
            user.addFriend(friendId);
            return user;
        }

        List<Boolean> isConfirmed = jdbcTemplate.query(sql, this::mapRowConfirmed, userId, friendId);
        if (isConfirmed.size() > 0) {
            if (!isConfirmed.get(0)) {
                log.error(String.format("Пользователь id = %d уже отправлял заявку пользователю id = %d",
                        userId, friendId));
                throw new ValidationException(String.format("Пользователь id = %d уже отправлял заявку пользователю id = %d",
                        userId, friendId));
            } else {
                log.error(String.format("Пользователь id = %d уже принял заявку пользователя id = %d",
                        friendId, userId));
                throw new ValidationException(String.format("Пользователь id = %d уже принял заявку пользователя id = %d",
                        friendId, userId));
            }
        }
        String sqlForInsert = "insert into friends values (?, ?, ?)";
        jdbcTemplate.update(sqlForInsert, userId, friendId, false);
        log.info(String.format("Пользователь id = %d отправил заявку в друзья пользователю id = %d",
                userId, friendId));
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
        String sql = "select * from users where id in (select friend_id from friends where user_id = ?)";
        return jdbcTemplate.query(sql, this::mapRowUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        List<User> userFriends = getFriends(userId);
        List<User> otherUserFriends = getFriends(otherId);
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toList());
    }

    private User mapRowUser(ResultSet rs, int rowNum) throws SQLException {
        int userId = rs.getInt("id");
        Set<Integer> userFriends = getFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return User.builder()
                .id(userId)
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friends(userFriends)
                .build();
    }

    private boolean mapRowConfirmed(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean("is_confirmed");
    }
}
