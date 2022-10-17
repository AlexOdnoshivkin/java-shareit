package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemToRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {
    private final EntityManager em;

    private final ItemRequestService itemRequestService;

    private ItemRequest itemRequest;
    private User user;

    private Item item;

    @BeforeEach
    void creteEntity() {
        itemRequest = new ItemRequest();
        itemRequest.setDescription("Test description");

        user = new User();
        user.setName("TestUser");
        user.setEmail("Test@gmail.com");
        em.persist(user);

        item = new Item();
        item.setAvailable(true);
        item.setName("TestItem");
        item.setDescription("Test description");
        em.persist(item);
    }

    @Test
    void addItemRequest() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        // Проверяем сценарий, когда пользователь не найден
        try {
            itemRequestService.addItemRequest(itemRequestDto, 100L);
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем корректную работу
        ItemRequestDto result = itemRequestService.addItemRequest(itemRequestDto, user.getId());

        item.setItemRequest(new ItemRequest());
        itemRequestDto.setOwner(user.getId());
        ItemToRequestDto itemToRequestDto = ItemMapper.toItemToRequestDto(item);
        itemRequestDto.setItems(List.of(itemToRequestDto));

        assertNotNull(result);
        assertEquals(itemRequestDto.getOwner(), result.getOwner());
    }

    @Test
    void getOwnRequestsByUser() {
        // Проверяем сценарий, когда пользователь не найден
        try {
            itemRequestService.getOwnRequestsByUser(100L);
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем корректный сценарий
        itemRequest.setOwnerId(user.getId());
        em.persist(itemRequest);
        List<ItemRequestDto> trueResult = List.of(ItemRequestMapper.toItemRequestDto(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getOwnRequestsByUser(user.getId());

        assertEquals(trueResult.size(), result.size());
        assertEquals(trueResult.get(0).getOwner(), result.get(0).getOwner());
    }

    @Test
    void getAllRequestsOtherUsers() {
        // Проверяем сценарий, когда пользователь не найден
        try {
            itemRequestService.getAllRequestsOtherUsers(0, 10, 100L);
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем корректный сценарий
        User user2 = new User();
        user2.setName("Test2User");
        user2.setEmail("Test2@gmail.com");
        em.persist(user2);
        itemRequest.setOwnerId(user2.getId());
        em.persist(itemRequest);
        List<ItemRequestDto> trueResult = List.of(ItemRequestMapper.toItemRequestDto(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getAllRequestsOtherUsers(0, 10, user.getId());

        assertEquals(trueResult.size(), result.size());
        assertEquals(trueResult.get(0).getOwner(), result.get(0).getOwner());
    }

    @Test
    void getRequestById() {
        itemRequest.setOwnerId(user.getId());
        em.persist(itemRequest);
        // Проверяем сценарий, когда пользователь не найден
        try {
            itemRequestService.getRequestById(100L, itemRequest.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем сценарий, когда запрос не найден
        try {
            itemRequestService.getRequestById(user.getId(), 100L);
        } catch (EntityNotFoundException e) {
            assertEquals("Запрос не найден", e.getMessage());
        }

        // Проверяем корректный сценарий
        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
    }
}