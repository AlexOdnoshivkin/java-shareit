package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Set;
import java.util.stream.Stream;

public interface ItemRepository {
    Stream<Item> findAll();

    Item getById(long itemId);

    Item add(Item item, long userId);

    Set<Long> getUserItems(long userId);


}
