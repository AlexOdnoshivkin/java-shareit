package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private UserMapper() {
        throw new AssertionError();
    }

    public static ru.practicum.shareit.user.dto.UserDto toUserDto(User user) {
        return new ru.practicum.shareit.user.dto.UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(ru.practicum.shareit.user.dto.UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }
}
