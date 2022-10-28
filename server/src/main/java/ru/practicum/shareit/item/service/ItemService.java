package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.stream.Stream;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto updatedItemDto, long itemId, long userId);

    ItemDto getById(Long itemId, Long userId);

    Item getItemById(Long itemId);

    Stream<ItemDto> searchItem(String text, Integer from, Integer size);

    Stream<ItemDto> getItems(Long userId, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
