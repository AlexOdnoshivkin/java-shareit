package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private ItemMapper() {
        throw new AssertionError();
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (item.getItemRequest() != null) {
            itemDto.setRequestId(item.getItemRequest().getId());
        }
        return itemDto;
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static ItemToRequestDto toItemToRequestDto(Item item) {
        ItemToRequestDto itemToRequestDto = new ItemToRequestDto();
        itemToRequestDto.setId(item.getId());
        itemToRequestDto.setName(item.getName());
        itemToRequestDto.setDescription(item.getDescription());
        itemToRequestDto.setAvailable(item.getAvailable());
        itemToRequestDto.setRequestId(item.getItemRequest().getId());
        return itemToRequestDto;
    }


}
