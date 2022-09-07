package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.NotFoundItemException;
import ru.practicum.shareit.user.NotFoundUserException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl service;
    private final Item item1 = new Item(1L, "Item1", "Description for item 1", true);
    private final User user1 = new User(1L, "User1", "user1@ya.ru");
    private final User user2 = new User(2L, "User2", "user2@ya.ru");
    private final Booking booking1 = new Booking(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    private final BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now());
    private final int from = 0;
    private final int size = 5;

    @Test
    public void test1_tryCreateNewBooking() {
        item1.setOwner(user2);
        bookingDto.setItemId(1L);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking1);
        Booking booking = service.createBooking(user1.getId(), bookingDto);
        Assertions.assertEquals(booking1, booking);
    }

    @Test
    public void test2_tryCreateNewBookingWhenItemIsNotFound() {
        bookingDto.setItemId(1L);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundItemException thrown = Assertions.assertThrows(NotFoundItemException.class, () ->
                service.createBooking(1L, bookingDto));
        Assertions.assertEquals("Вещь id = 1 не найдена", thrown.getMessage());
    }

    @Test
    public void test3_tryCreateNewBookingWhenBookerIsOwner() {
        bookingDto.setItemId(1L);
        item1.setOwner(user1);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        NotFoundUserException thrown = Assertions.assertThrows(NotFoundUserException.class, () ->
                service.createBooking(1L, bookingDto));
        Assertions.assertEquals("Невозможно бронирование собственное вещи пользователем id = 1", thrown.getMessage());
    }

    @Test
    public void test4_tryCreateNewBookingWhenItemIsNotAvailable() {
        bookingDto.setItemId(1L);
        item1.setOwner(user2);
        item1.setAvailable(false);
        Mockito
                .when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(item1));
        NotAvailableBookingException thrown = Assertions.assertThrows(NotAvailableBookingException.class, () ->
                service.createBooking(1L, bookingDto));
        Assertions.assertEquals("Вещь с id = 1 недоступна для бронирования", thrown.getMessage());
    }

    @Test
    public void test5_tryGetBookingWhenBookerIdIsUserId() {
        booking1.setId(1L);
        booking1.setBooker(user1);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        Booking booking = service.getBooking(booking1.getId(), user1.getId());
        Assertions.assertEquals(booking1, booking);
    }

    @Test
    public void test6_tryGetBookingWhenOwnerIdIsUserId() {
        booking1.setId(1L);
        booking1.setBooker(user2);
        item1.setOwner(user1);
        booking1.setItem(item1);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        Booking booking = service.getBooking(booking1.getId(), user1.getId());
        Assertions.assertEquals(booking1, booking);
    }

    @Test
    public void test7_tryGetBookingWhenUserIsNotOwnerAndBooker() {
        booking1.setId(1L);
        booking1.setBooker(user2);
        item1.setOwner(user2);
        booking1.setItem(item1);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        NotFoundBookingException thrown = Assertions.assertThrows(NotFoundBookingException.class, () ->
                service.getBooking(booking1.getId(), user1.getId()));
        Assertions.assertEquals("Пользователь id = 1 не может запрашивать информацию о бронировании id = 1",
                thrown.getMessage());
    }

    @Test
    public void test8_tryGetBookingWhenBookingIsNotFound() {
        booking1.setId(1L);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundBookingException thrown = Assertions.assertThrows(NotFoundBookingException.class, () ->
                service.getBooking(booking1.getId(), user1.getId()));
        Assertions.assertEquals("Бронирование id = 1 не найдено",
                thrown.getMessage());
    }

    @Test
    public void test9_tryChangeStatus() {
        item1.setOwner(user1);
        booking1.setId(1L);
        booking1.setItem(item1);
        booking1.setStatus(Status.REJECTED);
        Booking booking2 = new Booking();
        booking2.setStatus(Status.APPROVED);
        booking2.setItem(booking1.getItem());
        booking2.setId(booking1.getId());
        booking2.setStart(booking1.getStart());
        booking2.setEnd(booking1.getEnd());
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking2);
        Booking booking = service.changeStatus(booking1.getId(), true, user1.getId());
        Assertions.assertEquals(booking2, booking);
    }

    @Test
    public void test10_tryChangeStatusWhenBookingIsNotExist() {
        booking1.setId(1L);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundBookingException thrown = Assertions.assertThrows(NotFoundBookingException.class, () ->
                service.changeStatus(booking1.getId(), true, user1.getId()));
        Assertions.assertEquals("Бронирование id = 1 не найдено",
                thrown.getMessage());
    }

    @Test
    public void test11_tryChangeStatusWhenUserIsNotOwner() {
        item1.setOwner(user2);
        booking1.setId(1L);
        booking1.setItem(item1);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        NotFoundUserException thrown = Assertions.assertThrows(NotFoundUserException.class, () ->
                service.changeStatus(booking1.getId(), true, user1.getId()));
        Assertions.assertEquals("Данная вещь не принадлежит юзеру id = 1",
                thrown.getMessage());
    }

    @Test
    public void test12_tryChangeStatusWhenStatusIsApproved() {
        item1.setOwner(user1);
        booking1.setId(1L);
        booking1.setItem(item1);
        booking1.setStatus(Status.APPROVED);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        NotAvailableBookingException thrown = Assertions.assertThrows(NotAvailableBookingException.class, () ->
                service.changeStatus(booking1.getId(), true, user1.getId()));
        Assertions.assertEquals("Невозможно изменить статус на 'APPROVED'",
                thrown.getMessage());
    }

    @Test
    public void test13_tryChangeStatusWhenStatusIsRejected() {
        item1.setOwner(user1);
        booking1.setId(1L);
        booking1.setItem(item1);
        booking1.setStatus(Status.REJECTED);
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        NotAvailableBookingException thrown = Assertions.assertThrows(NotAvailableBookingException.class, () ->
                service.changeStatus(booking1.getId(), false, user1.getId()));
        Assertions.assertEquals("Невозможно изменить статус на 'REJECTED'",
                thrown.getMessage());
    }

    @Test
    public void test14_tryChangeStatusOnRejectedSuccess() {
        item1.setOwner(user1);
        booking1.setId(1L);
        booking1.setItem(item1);
        booking1.setStatus(Status.APPROVED);
        Booking booking2 = new Booking();
        booking2.setStatus(Status.REJECTED);
        booking2.setItem(booking1.getItem());
        booking2.setId(booking1.getId());
        booking2.setStart(booking1.getStart());
        booking2.setEnd(booking1.getEnd());
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(booking1));
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking2);
        Booking booking = service.changeStatus(booking1.getId(), false, user1.getId());
        Assertions.assertEquals(booking2, booking);
    }

    @Test
    public void test15_tryGetAllBookingWhenStateIsPast() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.PAST);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findByBooker_IdAndEndIsBefore(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test16_tryGetAllBookingWhenStateIsFuture() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.FUTURE);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findByBooker_IdAndStartIsAfter(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test17_tryGetAllBookingWhenStateIsCurrent() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CURRENT);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findCurrentBooking(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test18_tryGetAllBookingWhenStateIsAll() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.ALL);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findByBooker_Id(
                        Mockito.anyLong(),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test19_tryGetAllBookingWhenStateIsWaiting() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.WAITING);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByBookerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test20_tryGetAllBookingWhenStateIsApproved() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.APPROVED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByBookerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test21_tryGetAllBookingWhenStateIsRejected() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.REJECTED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByBookerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test22_tryGetAllBookingWhenStateIsCanceled() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByBookerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBooking(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test23_tryGetAllBookingWhenUserIsNotExist() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundUserException thrown = Assertions.assertThrows(NotFoundUserException.class, () ->
                service.getAllBooking(bookingDtoState, from, size));
        Assertions.assertEquals("Пользователь id = 1 не найден",
                thrown.getMessage());
    }

    @Test
    public void test24_tryGetAllBookingWhenSizeOrFromIsNotValid() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        ArithmeticException thrown1 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBooking(bookingDtoState, 0, 0));
        ArithmeticException thrown2 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBooking(bookingDtoState, -1, 0));
        ArithmeticException thrown3 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBooking(bookingDtoState, -1, 2));
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown1.getMessage());
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown2.getMessage());
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown3.getMessage());
    }

    @Test
    public void test25_tryGetAllBookingByOwnerWhenStateIsPast() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.PAST);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerPast(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test26_tryGetAllBookingByOwnerWhenStateIsFuture() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.FUTURE);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerFuture(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test27_tryGetAllBookingByOwnerWhenStateIsCurrent() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CURRENT);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerCurrent(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test28_tryGetAllBookingByOwnerWhenStateIsAll() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.ALL);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwner(
                        Mockito.anyLong(),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test29_tryGetAllBookingByOwnerWhenStateIsWaiting() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.WAITING);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test30_tryGetAllBookingByOwnerWhenStateIsApproved() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.APPROVED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test31_tryGetAllBookingByOwnerWhenStateIsRejected() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.REJECTED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test32_tryGetAllBookingByOwnerWhenStateIsCanceled() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").ascending());
        List<Booking> list = List.of(booking1);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Booking> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findBookingByOwnerAndStatus(
                        Mockito.anyLong(),
                        Mockito.any(Status.class),
                        Mockito.any(Pageable.class))
                )
                .thenReturn(page);
        List<Booking> result = service.getAllBookingByOwner(bookingDtoState, from, size);
        Assertions.assertEquals(List.of(booking1), result);
    }

    @Test
    public void test33_tryGetAllBookingWhenUserIsNotExist() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        NotFoundUserException thrown = Assertions.assertThrows(NotFoundUserException.class, () ->
                service.getAllBookingByOwner(bookingDtoState, from, size));
        Assertions.assertEquals("Пользователь id = 1 не найден",
                thrown.getMessage());
    }

    @Test
    public void test34_tryGetAllBookingWhenSizeOrFromIsNotValid() {
        BookingDtoState bookingDtoState = new BookingDtoState(user1.getId(), State.CANCELED);
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));
        ArithmeticException thrown1 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBookingByOwner(bookingDtoState, 0, 0));
        ArithmeticException thrown2 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBookingByOwner(bookingDtoState, -1, 0));
        ArithmeticException thrown3 = Assertions.assertThrows(ArithmeticException.class, () ->
                service.getAllBookingByOwner(bookingDtoState, -1, 2));
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown1.getMessage());
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown2.getMessage());
        Assertions.assertEquals("Ошибка в индекса первого элемента или количества элементов для отображения",
                thrown3.getMessage());
    }
}