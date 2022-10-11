package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ru.practicum.shareit.user.dto.UserDto postUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
        log.info("Получен запрос на добавление пользователя {}", userDto);
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ru.practicum.shareit.user.dto.UserDto patchUser(@Validated({Update.class}) @PathVariable long userId,
                                                           @RequestBody User user) {
        log.info("Получен запрос на обновление данных {} пользователя с id {}", user, userId);
        return userService.updateUser(userId, user);
    }

    @GetMapping()
    public List<ru.practicum.shareit.user.dto.UserDto> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers().collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public ru.practicum.shareit.user.dto.UserDto getUserById(@PathVariable long userId) {
        log.info("Получен запрос на получение пользователя с id {}", userId);
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.deleteUser(userId);
    }
}
