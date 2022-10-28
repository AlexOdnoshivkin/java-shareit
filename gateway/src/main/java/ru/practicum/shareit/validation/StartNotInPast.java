package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = StartNotInPastValidator.class)
@Documented
public @interface StartNotInPast {
    String message() default "Дата начала бронирования не может быть раньше текущего времени";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
