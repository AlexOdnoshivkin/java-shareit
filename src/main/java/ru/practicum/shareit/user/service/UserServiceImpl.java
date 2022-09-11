package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityAlreadyExistException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(User user) {
        checkIsEmailIsExist(user);
        return UserMapper.mapping(userRepository.add(user));
    }

    @Override
    public UserDto updateUser(long userId, User updatedUser) {
        if (userRepository.getById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        User user = userRepository.getById(userId);
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            checkIsEmailIsExist(updatedUser);
            user.setEmail(updatedUser.getEmail());
        }
        return UserMapper.mapping(userRepository.add(user));
    }

    @Override
    public UserDto getUserById(long userId) {
        if (userRepository.getById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        return UserMapper.mapping(userRepository.getById(userId));
    }

    @Override
    public Stream<UserDto> getAllUsers() {
        return userRepository.findAll().map(UserMapper::mapping);
    }

    @Override
    public void deleteUser(long userId) {
        if (userRepository.getById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        userRepository.delete(userId);
    }

    private void checkIsEmailIsExist(User user) {
        List<User> users = userRepository.findAll()
                .filter(savedUser -> savedUser.getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
        if (!users.isEmpty()) {
            throw new EntityAlreadyExistException("Указанный email уже существует");
        }
    }
}
