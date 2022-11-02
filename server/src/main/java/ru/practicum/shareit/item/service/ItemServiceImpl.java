package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.FromSizeRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        Item item = ItemMapper.toItem(itemDto);
        if (userService.getUserById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        item.setUserId(userId);
        if (itemDto.getRequestId() != null) {
            Optional<ItemRequest> optionalItemRequest =
                    itemRequestRepository.getItemRequestById(itemDto.getRequestId());
            if (optionalItemRequest.isEmpty()) {
                throw new EntityNotFoundException("Запрос не найден");
            }
            item.setItemRequest(optionalItemRequest.get());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto updatedItemDto, long itemId, long userId) {
        Item updatedItem = ItemMapper.toItem(updatedItemDto);
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        Set<Long> userItems = userService.getUserItems(userId).stream()
                .map(Item::getId)
                .collect(Collectors.toSet());
        if (userItems.isEmpty() || !userItems.contains(itemId)) {
            throw new EntityNotFoundException("Предмет отсутсвует у данного пользователя");
        }
        if (optionalItem.isEmpty()) {
            throw new EntityNotFoundException("Товар не найден");
        }
        Item item = optionalItem.get();
        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }


    @Override
    public ItemDto getById(Long itemId, Long userId) {
        log.info("На сервис поступил запрос на предмет с id {} от пользователя с id {}", itemId, userId);
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new EntityNotFoundException("Предмет не найден");
        }
        ItemDto itemDto = ItemMapper.toItemDto(optionalItem.get());
        itemDto.setComments(commentRepository.findCommentsByItemOrderByCreatedDesc(optionalItem.get())
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        if (userId.equals(optionalItem.get().getUserId())) {
            return setLastAndNextBookingForItem(itemDto);
        } else {
            return itemDto;
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new EntityNotFoundException("Предмет не найден");
        }
        return optionalItem.get();
    }

    @Override
    public Stream<ItemDto> searchItem(String text, Integer from, Integer size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        if (text.isEmpty()) {
            return Stream.empty();
        }
        return itemRepository.searchItemsBuNameAndDescription(text, pageable).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto);
    }

    @Override
    public Stream<ItemDto> getItems(Long userId, Integer from, Integer size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        if (userId == 0) {
            return getAllItems();
        } else {
            if (userService.getUserById(userId) == null) {
                throw new EntityNotFoundException("Пользователь не найден");
            }
            return itemRepository.findItemsByUserId(userId, pageable).stream()
                    .peek(System.out::println)
                    .map(ItemMapper::toItemDto)
                    .map(this::setLastAndNextBookingForItem);
        }
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        List<Booking> bookings = bookingRepository
                .findBookingsByBookerAndItemAndStatusNot(userId, itemId, BookingStatus.REJECTED);
        if (bookings.isEmpty()) {
            throw new IllegalStateException("У предмета не было бронирований");
        }
        boolean future = true;
        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(LocalDateTime.now())) {
                future = false;
                break;
            }
        }
        if (future) {
            throw new IllegalStateException("Комментарий не может быть оставлен к будущему бронированию");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(getItemById(itemId));
        comment.setAuthor(UserMapper.toUser(userService.getUserById(userId)));
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Stream<ItemDto> getAllItems() {
        return itemRepository.findAll().stream().map(ItemMapper::toItemDto);
    }

    private ItemDto setLastAndNextBookingForItem(ItemDto itemDto) {
        Booking lastBooking = null;
        Booking nextBooking = null;
        List<Booking> bookings = bookingRepository.findBookingsByItemAsc(itemDto.getId());
        log.info("BOOKING: {}", bookings);
        for (Booking booking : bookings) {
            if (booking.getStart().isAfter(LocalDateTime.now())) {
                nextBooking = booking;
                break;
            } else {
                lastBooking = booking;
            }
        }
        if (lastBooking != null) {
            itemDto.setLastBooking(BookingMapper.toBookingDto(lastBooking));
        }
        if (nextBooking != null) {
            itemDto.setNextBooking(BookingMapper.toBookingDto(nextBooking));
        }
        return itemDto;
    }
}
