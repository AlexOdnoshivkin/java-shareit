package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private UserMapper() {
        throw new AssertionError();
    }

    public static UserDto mapping(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
