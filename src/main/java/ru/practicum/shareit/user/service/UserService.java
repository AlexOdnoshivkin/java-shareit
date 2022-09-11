package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.stream.Stream;

public interface UserService {
    UserDto addUser(User user);

    UserDto updateUser(long userId, User updatedUser);

    UserDto getUserById(long userId);

    Stream<UserDto> getAllUsers();

    void deleteUser(long userId);
}
