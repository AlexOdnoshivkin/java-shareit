package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

import java.util.stream.Stream;

public interface UserRepository {
    Stream<User> findAll();

    User getById(long userId);

    User add(User user);

    void delete(long userId);
}
