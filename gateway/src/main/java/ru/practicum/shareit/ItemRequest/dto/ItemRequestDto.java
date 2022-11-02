package ru.practicum.shareit.ItemRequest.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;
    @NotEmpty
    private String description;
    private Long owner;
    private LocalDateTime created;
    private List<ItemDto> items = new ArrayList<>();
}
