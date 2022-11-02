package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.FromSizeRequest;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Override
    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        checkUserIsExist(userId);
        itemRequestDto.setOwner(userId);
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getOwnRequestsByUser(Long userId) {
        checkUserIsExist(userId);
        return itemRequestRepository.getItemRequestsByOwnerId(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequestsOtherUsers(Integer from, Integer size, Long userId) {
        checkUserIsExist(userId);
        Pageable pageable = FromSizeRequest.of(from, size);
        return itemRequestRepository.getOtherUserItemRequests(userId, pageable).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.getItemRequestById(requestId);
        checkUserIsExist(userId);

        if (optionalItemRequest.isEmpty()) {
            throw new EntityNotFoundException("Запрос не найден");
        }

        return ItemRequestMapper.toItemRequestDto(optionalItemRequest.get());
    }

    private void checkUserIsExist(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
    }
}
