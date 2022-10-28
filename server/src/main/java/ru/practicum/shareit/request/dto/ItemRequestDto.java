package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemToRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long owner;
    private LocalDateTime created;
    private List<ItemToRequestDto> items = new ArrayList<>();
}
