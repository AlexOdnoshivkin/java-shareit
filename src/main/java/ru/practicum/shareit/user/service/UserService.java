package ru.practicum.shareit.user.service;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Set;
import java.util.stream.Stream;

public interface UserService {
    ru.practicum.shareit.user.dto.UserDto addUser(ru.practicum.shareit.user.dto.UserDto userDto);

    ru.practicum.shareit.user.dto.UserDto updateUser(long userId, User updatedUser);

    ru.practicum.shareit.user.dto.UserDto getUserById(long userId);

    Stream<ru.practicum.shareit.user.dto.UserDto> getAllUsers();

    Set<Item> getUserItems(long userId);

    void deleteUser(long userId);
}
