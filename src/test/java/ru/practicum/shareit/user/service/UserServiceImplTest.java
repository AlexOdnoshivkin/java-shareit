package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private UserService userService;

    private UserRepository userRepository;

    private static UserDto userDto;
    private static User user;

    @BeforeEach
    void createEntity() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("TestUser");
        userDto.setEmail("user@gmail.com");

        user = new User();
        user.setId(1L);
        user.setName("TestUser");
        user.setEmail("user@gmail.com");

    }

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void addUser() {
        when(userRepository.save(any())).thenReturn(user);

        UserDto savedUser = userService.addUser(userDto);

        assertNotNull(savedUser);
        assertEquals(userDto, savedUser);

    }

    @Test
    void updateUserWhenUserNotFound() {
        UserDto updatedUserDto = new UserDto(); // Обновлённые данные юзера
        updatedUserDto.setId(1L);
        updatedUserDto.setEmail("update@gmail.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        userService.updateUser(updatedUserDto.getId(), updatedUserDto));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void updateUserWhenUpdateEmail() {
        // Обновляем почту
        UserDto updatedUserDto = new UserDto(); // Обновлённые данные юзера
        updatedUserDto.setId(1L);
        updatedUserDto.setEmail("update@gmail.com");
        User updatedUser = new User(); // оюновлённый юзер, возвращённый репозиторием
        updatedUser.setId(1L);
        updatedUser.setName(user.getName());
        updatedUser.setEmail(updatedUserDto.getEmail());
        when(userRepository.findById(updatedUserDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updatedUser);
        UserDto trueUserDto = new UserDto(); // Обновлённый юзер, которого мы ожидаем получить
        trueUserDto.setId(1L);
        trueUserDto.setName(user.getName());
        trueUserDto.setEmail(updatedUserDto.getEmail());

        UserDto savedUser = userService.updateUser(updatedUserDto.getId(), updatedUserDto);

        assertNotNull(savedUser);
        assertEquals(trueUserDto, savedUser);
    }

    @Test
    void updateUserWhenUpdateName() {
        // Обновляем почту
        UserDto updatedUserDto = new UserDto(); // Обновлённые данные юзера
        updatedUserDto.setId(1L);
        updatedUserDto.setName("updatedName");
        User updatedUser = new User(); // оюновлённый юзер, возвращённый репозиторием
        updatedUser.setId(1L);
        updatedUser.setName(updatedUserDto.getName());
        updatedUser.setEmail(user.getEmail());
        when(userRepository.findById(updatedUserDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(updatedUser);
        UserDto trueUserDto = new UserDto(); // Обновлённый юзер, которого мы ожидаем получить
        trueUserDto.setId(1L);
        trueUserDto.setName(updatedUserDto.getName());
        trueUserDto.setEmail(user.getEmail());

        UserDto savedUser = userService.updateUser(updatedUserDto.getId(), updatedUserDto);

        assertNotNull(savedUser);
        assertEquals(trueUserDto, savedUser);
    }

    @Test
    void getUserByIdWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        userService.getUserById(100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void getUserByIdWhenCorrectFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDto savedUser = userService.getUserById(1L);

        assertNotNull(savedUser);
        assertEquals(userDto.getId(), savedUser.getId());
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDto> trueResult = List.of(userDto);

        List<UserDto> result = userService.getAllUsers().collect(Collectors.toList());

        assertEquals(trueResult, result);
    }

    @Test
    void getUserItemsWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        userService.getUserItems(100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void getUserItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setUserId(1L);
        item1.setName("Test Item1");
        item1.setDescription("Test Item1 description");
        Item item2 = new Item();
        item2.setId(2L);
        item2.setUserId(1L);
        item2.setName("Test Item2");
        item2.setDescription("Test Item2 description");

        Set<Item> items = user.getUserItems();
        items.add(item1);
        items.add(item2);
        user.setUserItems(items);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Set<Item> returnedItems = userService.getUserItems(user.getId());

        assertEquals(items, returnedItems);
    }

    @Test
    void deleteUserWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        userService.deleteUser(100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void deleteUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(user.getId());
    }
}