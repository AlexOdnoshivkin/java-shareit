package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.stream.Stream;

public interface ItemService {
    ItemDto addItem(Item item, long userId);

    ItemDto updateItem(Item updatedItem, long itemId, long userId);

    Stream<ItemDto> getAllItems();

    Stream<ItemDto> getItemByUser(long userId);

    ItemDto getById(long itemId);

    Stream<ItemDto> searchItem(String text);
}
