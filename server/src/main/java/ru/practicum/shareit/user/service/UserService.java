package ru.practicum.shareit.user.service;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;
import java.util.stream.Stream;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(long userId, UserDto updatedUser);

    UserDto getUserById(long userId);

    Stream<UserDto> getAllUsers();

    Set<Item> getUserItems(long userId);

    void deleteUser(long userId);
}
