package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.NotFoundItemException;
import ru.practicum.shareit.user.NotFoundUserException;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking createBooking(long userId, BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundItemException(
                        String.format("Вещь id = %s не найдена", bookingDto.getItemId())));
        if (item.getOwner().getId() != userId) {
            if (item.getAvailable()) {
                Booking booking = BookingMapper.toBooking(bookingDto);
                booking.setStatus(Status.WAITING);
                booking.setItem(item);
                booking.setBooker(userRepository.findById(userId).orElseThrow());
                log.info("Пользователь id = {} бронирует вещь id = {}", userId, bookingDto.getItemId());
                return bookingRepository.save(booking);
            } else {
                throw new NotAvailableBookingException(String.format(
                        "Вещь с id = %s недоступна для бронирования",
                        bookingDto.getItemId()));
            }
        } else {
            throw new NotFoundUserException((String.format(
                    "Невозможно бронирование собственное вещи пользователем id = %s",
                    userId)));
        }
    }

    @Override
    public Booking getBooking(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundBookingException(
                        String.format("Бронирование id = %s не найдено", bookingId)));
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            log.info("Пользователь id = {} запрашивает информацию о бронировании id = {}", userId, bookingId);
            return bookingRepository.findById(bookingId).orElseThrow();
        } else {
            throw new NotFoundBookingException(
                    String.format("Пользователь id = %s не может запрашивать информацию о бронировании id = %s",
                            userId, bookingId));
        }
    }

    @Override
    public Booking changeStatus(long bookingId, boolean approved, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundBookingException(
                        String.format("Бронирование id = %s не найдено", bookingId)));
        if (booking.getItem().getOwner().getId() == userId) {
            if (approved) {
                if (booking.getStatus() != Status.APPROVED) {
                    booking.setStatus(Status.APPROVED);
                } else {
                    throw new NotAvailableBookingException(String.format(
                            "Невозможно изменить статус на '%s'",
                            Status.APPROVED));
                }
            } else if (booking.getStatus() != Status.REJECTED) {
                booking.setStatus(Status.REJECTED);
            } else {
                throw new NotAvailableBookingException(String.format(
                        "Невозможно изменить статус на '%s'",
                        Status.REJECTED));
            }
            log.info("Изменен статус бронирования id = {}", bookingId);
            return bookingRepository.save(booking);
        } else {
            throw new NotFoundUserException(String.format("Данная вещь не принадлежит юзеру id = %s", userId));
        }
    }

    @Override
    public List<Booking> getAllBooking(BookingDtoState bookingDtoState, int from, int size) {
        if (userRepository.findById(bookingDtoState.getUserId()).isPresent()) {
            int page = from / size;
            Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());
            log.info("Запрошены бронирования пользователя id = {}", bookingDtoState.getUserId());
            switch (bookingDtoState.getState()) {
                case PAST:
                    return bookingRepository.findByBooker_IdAndEndIsBefore(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case FUTURE:
                    return bookingRepository.findByBooker_IdAndStartIsAfter(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case CURRENT:
                    return bookingRepository.findCurrentBooking(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case ALL:
                    return bookingRepository.findByBooker_Id(bookingDtoState.getUserId(), pageable).getContent();
                default:
                    return bookingRepository.findBookingByBookerAndStatus(
                            bookingDtoState.getUserId(),
                            Status.valueOf(bookingDtoState.getState().toString()),
                            pageable).getContent();
            }
        } else {
            throw new NotFoundUserException(String.format("Пользователь id = %s не найден", bookingDtoState.getUserId()));
        }
    }

    @Override
    public List<Booking> getAllBookingByOwner(BookingDtoState bookingDtoState, int from, int size) {
        if (userRepository.findById(bookingDtoState.getUserId()).isPresent()) {
            int page = from / size;
            Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());
            log.info("Запрошены бронирования вещей, принадлежащих пользователю id = {}", bookingDtoState.getUserId());
            switch (bookingDtoState.getState()) {
                case PAST:
                    return bookingRepository.findBookingByOwnerPast(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case FUTURE:
                    return bookingRepository.findBookingByOwnerFuture(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case CURRENT:
                    return bookingRepository.findBookingByOwnerCurrent(bookingDtoState.getUserId(),
                            LocalDateTime.now(), pageable).getContent();
                case ALL:
                    return bookingRepository.findBookingByOwner(bookingDtoState.getUserId(), pageable).getContent();
                default:
                    return bookingRepository.findBookingByOwnerAndStatus(
                            bookingDtoState.getUserId(),
                            Status.valueOf(bookingDtoState.getState().toString()), pageable).getContent();
            }
        } else {
            throw new NotFoundUserException(String.format("Пользователь id = %s не найден", bookingDtoState.getUserId()));
        }
    }
}