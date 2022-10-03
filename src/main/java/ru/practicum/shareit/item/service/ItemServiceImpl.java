package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        Item item = ItemMapper.toItem(itemDto);
        if (userService.getUserById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        item.setUserId(userId);
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
    public Stream<ItemDto> searchItem(String text) {
        if (text.isEmpty()) {
            return Stream.empty();
        }
        Stream<Item> findByName = itemRepository.findItemByNameContainsIgnoreCase(text).stream();
        Stream<Item> findByDescription = itemRepository.findItemByDescriptionContainsIgnoreCase(text).stream();
        return Stream.concat(findByDescription, findByName)
                .distinct()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto);
    }

    @Override
    public Stream<ItemDto> getItems(long userId) {
        if (userId == 0) {
            return getAllItems();
        } else {
            if (userService.getUserById(userId) == null) {
                throw new EntityNotFoundException("Пользователь не найден");
            }
            return getItemByUser(userId);
        }
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        List<Booking> bookings = bookingRepository
                .findBookingByBookerIdAndItemIdAndStatusNot(userId, itemId, BookingStatus.REJECTED);
        if (bookings.isEmpty()) {
            throw new IllegalStateException("У предмета не было бронирований");
        }
        boolean future = true;
        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(LocalDateTime.now())) {
                future = false;
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

    private Stream<ItemDto> getItemByUser(long userId) {
        return userService.getUserItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .sorted(Comparator.comparing(ItemDto::getId))
                .map((this::setLastAndNextBookingForItem));
    }

    private ItemDto setLastAndNextBookingForItem(ItemDto itemDto) {
        Booking lastBooking = null;
        Booking nextBooking = null;
        List<Booking> bookings = bookingRepository.findBookingsByItemIdOrderByStartAsc(itemDto.getId());
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            if (booking.getStart().isAfter(LocalDateTime.now())) {
                nextBooking = booking;
                if (i != 0) {
                    lastBooking = bookings.get(i - 1);
                }
                break;
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
