package ru.practicum.shareit.item.dto;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class ItemDto {
    private final long id;
    private final String name;
    private final String description;
    private final boolean available;
}
