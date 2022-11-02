package ru.practicum.shareit.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class StartNotInPastValidator implements ConstraintValidator<StartNotInPast, LocalDateTime> {
    private final LocalDateTime now = LocalDateTime.now();

    @Override
    public void initialize(StartNotInPast constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime startTime, ConstraintValidatorContext context) {
        return startTime.isAfter(now);
    }
}
