package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService itemService;
    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void creteEntity() {
        user = new User();
        user.setName("TestUser");
        user.setEmail("Test@gmail.com");
        em.persist(user);

        item = new Item();
        item.setAvailable(true);
        item.setName("TestItem");
        item.setDescription("Test description");

        itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setName("TestItem");
        itemDto.setDescription("Test description");
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table items");
    }


    @Test
    void addItem() {
        // Проверяем случай, если пользователь не найден
        try {
            itemService.addItem(itemDto, 100L);
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем случай, если запрос к предмету не найден
        itemDto.setRequestId(100L);
        try {
            itemService.addItem(itemDto, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Запрос не найден", e.getMessage());
            itemDto.setRequestId(null);
        }

        // Проверяем успешный сценарий
        ItemDto savedItem = itemService.addItem(itemDto, user.getId());

        itemDto.setId(savedItem.getId());

        assertEquals(itemDto, savedItem);
    }

    @Test
    void updateItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setName("TestItem");
        itemDto.setDescription("Test description");

        // Проверяем случай, ecли предмет не найден
        try {
            itemService.addItem(itemDto, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Товар не найден", e.getMessage());
        }

        em.persist(item);
        itemDto.setId(item.getId());

        // Проверяем случай, если предмет не найден у пользователя
        try {
            itemService.addItem(itemDto, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Предмет отсутсвует у данного пользователя", e.getMessage());
        }

        Set<Item> items = Set.of(item);
        user.setUserItems(items);

        // Проверка обновления имени
        itemDto.setName("UpdateName");
        ItemDto updatedItemDto = itemService.updateItem(itemDto, item.getId(), user.getId());
        assertEquals(itemDto, updatedItemDto);

        // Проверка обновления описания
        itemDto.setDescription("Update description");
        updatedItemDto = itemService.updateItem(itemDto, item.getId(), user.getId());
        assertEquals(itemDto, updatedItemDto);

        // Проверка обновления статуса
        itemDto.setAvailable(false);
        updatedItemDto = itemService.updateItem(itemDto, item.getId(), user.getId());
        assertEquals(itemDto, updatedItemDto);
    }

    @Test
    void getById() {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setText("Test comment");

        // Проверяем случай, ecли предмет не найден
        try {
            itemService.getById(100L, user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Предмет не найден", e.getMessage());
        }

        // Проверяем корректное выполнение
        em.persist(item);
        em.persist(comment);
        List<CommentDto> comments = List.of(CommentMapper.toCommentDto(comment));
        itemDto.setComments(comments);
        itemDto.setId(item.getId());
        Set<Item> items = Set.of(item);
        user.setUserItems(items);

        ItemDto savedItem = itemService.getById(item.getId(), user.getId());

        assertNotNull(savedItem);
        assertEquals(itemDto, savedItem);
    }

    @Test
    void getItemById() {
        // Проверяем случай, ecли предмет не найден
        try {
            itemService.getItemById(user.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Предмет не найден", e.getMessage());
        }

        // Проверяем корректное выполнение
        em.persist(item);

        Item savedItem = itemService.getItemById(item.getId());

        assertNotNull(savedItem);
        assertEquals(item, savedItem);
    }

    @Test
    void searchItem() {
        Stream<ItemDto> emptyStream = Stream.empty();

        // Проверяем, если строка поиска пуста
        Stream<ItemDto> returnedStream = itemService.searchItem("", 0, 10);

        assertEquals((int) emptyStream.count(), (int) returnedStream.count());

        // Проверяем корректную работу
        em.persist(item);
        itemDto.setId(item.getId());
        List<ItemDto> trueResult = List.of(itemDto);

        List<ItemDto> result = itemService.searchItem("TeSt", 0, 10).collect(Collectors.toList());

        assertEquals(trueResult, result);

        // Проверяем, если не найдено совпадений
        result = itemService.searchItem("Spring", 0, 10).collect(Collectors.toList());

        assertEquals(0, result.size());
    }

    @Test
    void getItems() {
        Item item2 = new Item();
        item2.setAvailable(true);
        item2.setName("TestItem2");
        item2.setDescription("Test description item2");

        User user2 = new User();
        user2.setName("TestUser2");
        user2.setEmail("Test2@gmail.com");
        List<ItemDto> trueResult = new ArrayList<>();
        itemDto.setId(item.getId());

        user.setUserItems(Set.of(item));
        user2.setUserItems(Set.of(item2));
        em.persist(user);
        em.persist(user2);
        item.setUserId(user.getId());
        item2.setUserId(user2.getId());
        em.persist(item);
        em.persist(item2);

        ItemDto itemDto2 = ItemMapper.toItemDto(item2);
        itemDto.setId(item.getId());
        trueResult.add(itemDto);
        trueResult.add(itemDto2);

        // Проверяем, если userId == 0
        List<ItemDto> result = itemService.getItems(0L, 0, 10).collect(Collectors.toList());

        assertEquals(trueResult, result);

        // Проверяем, если пользователь не найден
        try {
            itemService.getItems(100L, 0, 10);
        } catch (EntityNotFoundException e) {
            assertEquals("Пользователь не найден", e.getMessage());
        }

        // Проверяем, если userId == 1
        trueResult.clear();
        trueResult.add(itemDto);

        result = itemService.getItems(user.getId(), 0, 10).collect(Collectors.toList());

        assertEquals(trueResult, result);
    }

    @Test
    void addComment() {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setText("Test comment");

        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        em.persist(item);

        // Проверяем случай, когда у предмета нет пробинрований
        try {
            itemService.addComment(user.getId(), item.getId(), commentDto);
        } catch (IllegalStateException e) {
            assertEquals("У предмета не было бронирований", e.getMessage());
        }

        // Проверяем корректный возврат
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(21));
        booking.setEnd(LocalDateTime.now().minusDays(10));
        em.persist(booking);

        CommentDto result = itemService.addComment(user.getId(), item.getId(), commentDto);

        commentDto.setId(1L);

        assertNotNull(result);
        assertEquals(commentDto.getId(), result.getId());
        assertEquals(commentDto.getText(), result.getText());

        // Проверяем случай, когда комментарий оставлен к бронированию в будущем
        booking.setStart(LocalDateTime.now().plusDays(10));
        booking.setEnd(LocalDateTime.now().plusDays(20));
        em.persist(booking);

        try {
            itemService.addComment(user.getId(), item.getId(), commentDto);
        } catch (IllegalStateException e) {
            assertEquals("Комментарий не может быть оставлен к будущему бронированию", e.getMessage());
        }
    }
}