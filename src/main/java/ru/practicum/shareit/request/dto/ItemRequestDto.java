package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemToRequestDto;

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
    private List<ItemToRequestDto> items = new ArrayList<>();
}
