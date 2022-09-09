package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final HashMap<Long, Item> items = new HashMap<>();
    private final HashMap<Long, Set<Long>> userItems = new HashMap<>();

    private long count;

    @Override
    public Stream<Item> findAll() {
        log.debug("Получены все предметы {}", items.values());
        return items.values().stream();
    }

    @Override
    public Item getById(long itemId) {
        log.debug("Получен предмет с id {}", itemId);
        return items.get(itemId);
    }

    @Override
    public Item add(Item item, long userId) {
        Set<Long> savedUserItems;
        if (userItems.get(userId) != null) {
            savedUserItems = userItems.get(userId);
        } else {
            savedUserItems = new HashSet<>();
        }
        if (item.getId() == 0) {
            item.setId(++count);
        }
        items.put(item.getId(), item);
        log.debug("Добавлен предмет{}", item);

        savedUserItems.add(item.getId());
        userItems.put(userId, savedUserItems);
        log.debug("Добавлен предмет с id {} пользователю с id {}", item.getId(), userId);
        return items.get(item.getId());
    }

    @Override
    public Set<Long> getUserItems(long userId) {
        return userItems.get(userId);
    }
}
