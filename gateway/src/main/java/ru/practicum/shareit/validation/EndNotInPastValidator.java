package ru.practicum.shareit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class EndNotInPastValidator implements ConstraintValidator<EndNotInPast, LocalDateTime> {
    private final LocalDateTime now = LocalDateTime.now();

    @Override
    public void initialize(EndNotInPast endNotInPast) {
    }

    @Override
    public boolean isValid(LocalDateTime endTime, ConstraintValidatorContext context) {
        return endTime.isAfter(now);
    }
}
