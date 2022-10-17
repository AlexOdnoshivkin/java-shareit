package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
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
    void addItemRequestWhenUserNotFound() {
        // Проверяем сценарий, когда пользователь не найден
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        itemRequestService.addItemRequest(itemRequestDto, 100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void addItemRequestWhenCorrect() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        ItemRequestDto result = itemRequestService.addItemRequest(itemRequestDto, user.getId());

        item.setItemRequest(new ItemRequest());
        itemRequestDto.setOwner(user.getId());

        assertNotNull(result);
        assertEquals(itemRequestDto.getOwner(), result.getOwner());
    }

    @Test
    void getOwnRequestsByUserWhenUserNotFound() {
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        itemRequestService.getOwnRequestsByUser(100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void getOwnRequestsByUserWhenCorrect() {
        // Проверяем корректный сценарий
        itemRequest.setOwnerId(user.getId());
        em.persist(itemRequest);
        List<ItemRequestDto> trueResult = List.of(ItemRequestMapper.toItemRequestDto(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getOwnRequestsByUser(user.getId());

        assertEquals(trueResult.size(), result.size());
        assertEquals(trueResult.get(0).getOwner(), result.get(0).getOwner());
    }

    @Test
    void getAllRequestsOtherUsersWhenUserNotFound() {
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        itemRequestService.getAllRequestsOtherUsers(0, 10, 100L));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void getAllRequestsOtherUsersWhenCorrect() {
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
    void getRequestByIdWhenUserNotFound() {
        // Проверяем сценарий, когда пользователь не найден
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        itemRequestService.getRequestById(100L, itemRequest.getId()));

        assertEquals(thrown.getMessage(), "Пользователь не найден");
    }

    @Test
    void getRequestByIdWhenRequestNotFound() {
        // Проверяем сценарий, когда запрос не найден
        EntityNotFoundException thrown = Assertions
                .assertThrows(EntityNotFoundException.class, () ->
                        itemRequestService.getRequestById(user.getId(), 100L));

        assertEquals(thrown.getMessage(), "Запрос не найден");
    }

    @Test
    void getRequestByIdWhenCorrect() {
        // Проверяем корректный сценарий
        itemRequest.setOwnerId(user.getId());
        em.persist(itemRequest);

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
    }
}