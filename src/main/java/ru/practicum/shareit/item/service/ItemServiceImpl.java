package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addItem(Item item, long userId) {
        if (userService.getUserById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        return ItemMapper.mapping(itemRepository.add(item, userId));
    }

    @Override
    public ItemDto updateItem(Item updatedItem, long itemId, long userId) {
        Item item = itemRepository.getById(itemId);
        Set<Long> userItems = itemRepository.getUserItems(userId);
        if (userItems == null || !userItems.contains(itemId)) {
            throw new EntityNotFoundException("Предмет отсутсвует у данного пользователя");
        }
        if (item == null) {
            throw new EntityNotFoundException("Товар не найден");
        }
        if (userService.getUserById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        return ItemMapper.mapping(itemRepository.add(item, userId));
    }

    @Override
    public Stream<ItemDto> getAllItems() {
        return itemRepository.findAll().map(ItemMapper::mapping);
    }

    @Override
    public Stream<ItemDto> getItemByUser(long userId) {
        return itemRepository.getUserItems(userId).stream()
                .map(itemRepository::getById)
                .map(ItemMapper::mapping);
    }

    @Override
    public ItemDto getById(long itemId) {
        return ItemMapper.mapping(itemRepository.getById(itemId));
    }

    @Override
    public Stream<ItemDto> searchItem(String text) {
        if (text.isEmpty()) {
            return Stream.empty();
        }
        Stream<Item> findByName = itemRepository.findAll()
                .filter((Item item) -> item.getName()
                        .toLowerCase().contains(text.toLowerCase()));
        Stream<Item> findByDescription = itemRepository.findAll()
                .filter((Item item) -> item.getDescription()
                        .toLowerCase().contains(text.toLowerCase()));
        return Stream.concat(findByDescription, findByName)
                .distinct()
                .filter(Item::getAvailable)
                .map(ItemMapper::mapping);
    }
}
