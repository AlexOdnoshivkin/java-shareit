package ru.practicum.shareit.ItemRequest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.ItemRequest.dto.ItemRequestDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> postRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @Validated @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен Post-запрос на создание запроса пользователем с id {}", userId);
        return itemRequestClient.postRequest(userId, itemRequestDto);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllOwnRequestByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получен Get-запрос списка запросов пользователя с id {}", userId);
        return itemRequestClient.getAllOwnRequestByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestsOtherUsers(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                         Integer from,
                                                         @Positive @RequestParam(name = "size", defaultValue = "10")
                                                         Integer size) {
        log.info("Получен Get-запрос списка запросов друших пользователей от пользователя с id {}," +
                "c from {} и size {}", userId, from, size);
        return itemRequestClient.getAllRequestsOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long requestId) {
        log.info("Получен Get-запрос запроса с id {}", requestId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
