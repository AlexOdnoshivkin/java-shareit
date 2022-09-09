package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping()
    public UserDto postUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя {}", user);
        return userService.addUser(user);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@Valid @PathVariable long userId, @RequestBody User user) {
        log.info("Получен запрос на обновление данных {} пользователя с id {}", user, userId);
        return userService.updateUser(userId, user);
    }

    @GetMapping()
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers().collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        log.info("Получен запрос на получение пользователя с id {}", userId);
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.deleteUser(userId);
    }
}
