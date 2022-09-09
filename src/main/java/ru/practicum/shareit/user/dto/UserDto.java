package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class UserDto {
    private final long id;
    @NotBlank
    private final String name;
    @NotEmpty
    @Email
    private final String email;
}
