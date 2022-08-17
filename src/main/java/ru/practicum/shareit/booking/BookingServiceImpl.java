package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.NotFoundItemException;
import ru.practicum.shareit.user.NotFoundUserException;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                    "Невозможно бронирование собственное вещи пользователем %s",
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
                    String.format("Пользователь %s не может запрашивать информацию о бронировании %s",
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
    public List<Booking> getAllBooking(BookingDtoState bookingDtoState) {
        if (userRepository.findById(bookingDtoState.getUserId()).isPresent()) {
            log.info("Запрошены бронирования пользователя id = {}", bookingDtoState.getUserId());
            switch (bookingDtoState.getState()) {
                case PAST:
                    return bookingRepository.findByBooker_IdAndEndIsBefore(bookingDtoState.getUserId(),
                                    LocalDateTime.now()).stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.findByBooker_IdAndStartIsAfter(bookingDtoState.getUserId(),
                                    LocalDateTime.now()).stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository.findCurrentBooking(bookingDtoState.getUserId(), LocalDateTime.now());
                case ALL:
                    return bookingRepository.findByBooker_Id(bookingDtoState.getUserId()).stream()
                            .sorted(Comparator.comparing(Booking::getStart).reversed())
                            .collect(Collectors.toList());
                default:
                    return bookingRepository.findBookingByBookerAndStatus
                            (bookingDtoState.getUserId(),
                                    Status.valueOf(bookingDtoState.getState().toString()));
            }
        } else {
            throw new NotFoundUserException(String.format("Пользователь id = %s не найден", bookingDtoState.getUserId()));
        }
    }

    @Override
    public List<Booking> getAllBookingByOwner(BookingDtoState bookingDtoState) {
        if (userRepository.findById(bookingDtoState.getUserId()).isPresent()) {
            log.info("Запрошены бронирования вещей, принадлежащих пользователю id = {}", bookingDtoState.getUserId());
            switch (bookingDtoState.getState()) {
                case PAST:
                    return bookingRepository.findBookingByOwnerPast(bookingDtoState.getUserId(),
                            LocalDateTime.now());
                case FUTURE:
                    return bookingRepository.findBookingByOwnerFuture(bookingDtoState.getUserId(),
                            LocalDateTime.now());
                case CURRENT:
                    return bookingRepository.findBookingByOwnerCurrent(bookingDtoState.getUserId(),
                            LocalDateTime.now());
                case ALL:
                    return bookingRepository.findBookingByOwner(bookingDtoState.getUserId());
                default:
                    return bookingRepository.findBookingByOwnerAndStatus(
                            bookingDtoState.getUserId(),
                            Status.valueOf(bookingDtoState.getState().toString()));
            }
        } else {
            throw new NotFoundUserException(String.format("Пользователь id = %s не найден", bookingDtoState.getUserId()));
        }
    }
}