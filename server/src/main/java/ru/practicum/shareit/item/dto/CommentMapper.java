package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Comment;

public class CommentMapper {
    //Подавление конструктора по умолчанию для достижения неинстанцируемости
    private CommentMapper() {
        throw new AssertionError();
    }

    public static Comment toComment(CommentDto commentDto) {
        return new Comment(commentDto.getId(), commentDto.getText(), null, null, commentDto.getCreated());
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getItem().getUserId(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }

}
