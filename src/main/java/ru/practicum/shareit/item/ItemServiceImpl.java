package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.NotAvailableBookingException;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoFromRequest;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.requests.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public Item createItem(ItemDto itemDto, long userId) {
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.findById(userId).orElseThrow());
        if (itemDto.getRequestId() != null) {
            item.setRequest(itemRequestRepository.findById(itemDto.getRequestId()).get());
        }
        log.info("Добавлена вещь {}", item.getName());
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(long userId, ItemDto itemDto, long itemId) {
        Item item = ItemMapper.toItem(itemDto);
        Item updateItem = itemRepository.findById(itemId).orElseThrow();
        if (updateItem.getOwner().equals(userRepository.findById(userId).orElseThrow())) {
            updateFields(item, updateItem, itemDto);
            log.info("Обновлена вещь id = {}", item.getId());
            return itemRepository.save(updateItem);
        } else {
            throw new NotFoundItemException(String.format("У пользователя с id = %s нет вещи с id = %s",
                    userId, itemId));
        }
    }

    @Override
    public ItemDtoWithBooking getItem(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundItemException(String.format("Вещь с id = %s не найдена", itemId)));
        ItemDtoWithBooking itemDtoWithBooking = ItemMapper.toItemDtoWithBooking(item);
        if (item.getOwner().getId() == userId) {
            setBookings(itemDtoWithBooking, itemId);
        }
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        if (!comments.isEmpty()) {
            itemDtoWithBooking.setComments(
                    comments.stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toSet()));
        }
        log.info("Запрошена вещь id = {}", itemId);
        return itemDtoWithBooking;
    }

    @Override
    public Collection<ItemDtoWithBooking> getAllItem(long userId, int from, int size) {
        if (size > 0 && from >= 0) {
            int page = from / size;
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            List<Item> items = itemRepository.findAllByOwnerId(userId, pageable).getContent();
            List<ItemDtoWithBooking> itemsDto = items.stream()
                    .map(ItemMapper::toItemDtoWithBooking)
                    .collect(Collectors.toList());
            for (int i = 0; i < itemsDto.size(); i++) {
                setBookings(itemsDto.get(i), items.get(i).getId());
                Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemsDto.get(i).getId());
                if (!comments.isEmpty()) {
                    itemsDto.get(i).setComments(
                            comments.stream()
                                    .map(CommentMapper::toCommentDto)
                                    .collect(Collectors.toSet()));
                }
            }
            log.info("Запрошены вещи пользователя id = {}", userId);
            return itemsDto;
        } else {
            throw new ArithmeticException("Неверное значение индекса первого элемента или количества элементов для отображения");
        }
    }

    @Override
    public Collection<Item> searchItems(String text, int from, int size) {
        if (!text.isEmpty()) {
            if (size > 0 && from >= 0) {
                int page = from / size;
                Pageable pageable = PageRequest.of(page, size);
                log.info("Выполнен поиск вещей по тексту: '{}'", text);
                return itemRepository.findItems(text, pageable).getContent();
            } else {
                throw new ArithmeticException(
                        "Неверное значение индекса первого элемента или количества элементов для отображения"
                );
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Comment createComment(long userId, long itemId, CommentDtoFromRequest comment) {
        List<Booking> booking = bookingRepository.findByBooker_IdAndEndIsBefore(userId, LocalDateTime.now());
        if (booking.stream().anyMatch(s -> s.getItem().getId() == itemId)) {
            Comment newComment = new Comment();
            newComment.setItem(itemRepository.findById(itemId).get());
            newComment.setAuthor(userRepository.findById(userId).get());
            newComment.setText(comment.getText());
            log.info("Добавлен отзыв на вещь id = {}", itemId);
            return commentRepository.save(newComment);
        } else {
            throw new NotAvailableBookingException(String.format(
                    "Пользователь id = %s не брал в аренду вещь id = %s, или аренда еще не завершена",
                    userId, itemId));
        }
    }

    private void updateFields(Item item, Item updateItem, ItemDto itemDto) {
        if (item.getName() != null) {
            updateItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            item.setRequest(itemRequestRepository.findById(itemDto.getRequestId()).orElseThrow());
        }
    }

    private void setBookings(ItemDtoWithBooking itemDtoWithBooking, long itemId) {
        Booking lastBooking = bookingRepository.findLastItemBooking(itemId, LocalDateTime.now());
        if (lastBooking != null) {
            itemDtoWithBooking.setLastBooking(BookingMapper.toBookingDtoForItem(lastBooking));
        }
        Booking nextBooking = bookingRepository.findNextItemBooking(itemId, LocalDateTime.now());
        if (nextBooking != null) {
            itemDtoWithBooking.setNextBooking(BookingMapper.toBookingDtoForItem(nextBooking));
        }
    }
}