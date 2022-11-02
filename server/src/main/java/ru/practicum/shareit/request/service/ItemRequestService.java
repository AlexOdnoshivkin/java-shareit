package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getOwnRequestsByUser(Long userId);

    List<ItemRequestDto> getAllRequestsOtherUsers(Integer from, Integer size, Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
