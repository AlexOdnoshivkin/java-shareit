package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private ItemMapper() {
        throw new AssertionError();
    }

    public static ItemDto mapping(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }
}
