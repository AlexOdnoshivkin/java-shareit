package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.stream.Stream;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();
    private long count;

    @Override
    public Stream<User> findAll() {
        log.debug("Получены все пользователи {}", users.values());
        return users.values().stream();
    }

    @Override
    public User getById(long userId) {
        log.debug("Получен пользователь c id {}", userId);
        return users.get(userId);
    }

    @Override
    public User add(User user) {
        if (user.getId() == 0) {
            user.setId(++count);
        }
        users.put(user.getId(), user);
        log.debug("Добавлен пользователь {}", user);
        return getById(user.getId());
    }

    @Override
    public void delete(long userId) {
        log.debug("Удалён пользователь c id {}", userId);
        users.remove(userId);
    }
}
