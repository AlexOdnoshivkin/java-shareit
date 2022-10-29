package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto postRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен Post-запрос на создание запроса пользователем с id {}", userId);
        return itemRequestService.addItemRequest(itemRequestDto, userId);
    }

    @GetMapping()
    public List<ItemRequestDto> getAllOwnRequestByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получен Get-запрос списка запросов пользователя с id {}", userId);
        return itemRequestService.getOwnRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequestsOtherUsers(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @RequestParam(name = "from", defaultValue = "0")
                                                         Integer from,
                                                         @RequestParam(name = "size", defaultValue = "10")
                                                         Integer size) {
        log.info("Получен Get-запрос списка запросов друших пользователей от пользователя с id {}," +
                "c from {} и size {}", userId, from, size);
        return itemRequestService.getAllRequestsOtherUsers(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long requestId) {
        log.info("Получен Get-запрос запроса с id {}", requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
