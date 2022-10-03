package ru.practicum.shareit.exceptions;

public class EntityNotAvailableException extends RuntimeException {
    public EntityNotAvailableException(String message) {
        super(message);
    }
}
