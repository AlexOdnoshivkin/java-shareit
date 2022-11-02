package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    @NotEmpty
    private String text;
    private Long itemId;
    private String authorName;
    private LocalDateTime created;
}