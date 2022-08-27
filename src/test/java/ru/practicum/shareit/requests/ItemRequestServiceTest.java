package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:test.properties")
public class ItemRequestServiceTest {
    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;
    private final ItemService itemService;
    private final User user1 = new User(1L, "Test1", "test1@email.com");
    private final User user2 = new User(2L, "Test2", "test2@email.com");
    private final ItemRequest itemRequest1 = new ItemRequest();
    private final ItemRequest itemRequest2 = new ItemRequest();
    private final Item item = new Item(1L, "New item", "Item for test", true);

    @BeforeEach
    public void restartIdentity() {
        em.createNativeQuery("TRUNCATE table users restart identity CASCADE;").executeUpdate();
        itemRequest1.setDescription("Test request 1");
        itemRequest2.setDescription("Test request 2");
    }

    @Test
    public void test1_createRequest() {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@email.com");
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Test description");
        userService.createUser(user);
        service.createRequest(1, itemRequest);
        TypedQuery<ItemRequest> query = em.createQuery(
                "select i from ItemRequest i where i.description = :description",
                ItemRequest.class);
        ItemRequest newItemRequest = query.setParameter("description", itemRequest.getDescription()).getSingleResult();
        assertThat(newItemRequest.getId(), notNullValue());
        assertThat(newItemRequest.getId(), equalTo(1L));
        assertThat(newItemRequest.getRequestor(), equalTo(user));
        assertThat(newItemRequest.getCreated(), notNullValue());
        assertThat(newItemRequest.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    public void test2_getRequestsByOwner() {
        item.setRequest(itemRequest1);
        userService.createUser(user1);
        userService.createUser(user2);
        service.createRequest(1, itemRequest1);
        service.createRequest(1, itemRequest2);
        itemService.createItem(ItemMapper.toItemDto(item), 2L);
        List<ItemRequestDto> requests = service.getRequestsByOwner(user1.getId());
        List<ItemRequestDto> expectedRequests = Stream.of(itemRequest1, itemRequest2)
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        expectedRequests.forEach(s -> {
            if (s.getId().equals(item.getRequest().getId())) {
                s.setItems(Set.of(ItemMapper.toItemDto(item)));
            }
        });
        assertThat(requests.size(), equalTo(2));
        assertThat(requests, equalTo(expectedRequests));
    }

    @Test
    public void test3_getAllRequest() {
        item.setRequest(itemRequest1);
        userService.createUser(user1);
        userService.createUser(user2);
        service.createRequest(1, itemRequest1);
        service.createRequest(1, itemRequest2);
        itemService.createItem(ItemMapper.toItemDto(item), 2L);
        List<ItemRequestDto> requests = service.getAllRequest(2L, 0, 5);
        List<ItemRequestDto> expectedRequests = Stream.of(itemRequest1, itemRequest2)
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        expectedRequests.forEach(s -> {
            if (s.getId().equals(item.getRequest().getId())) {
                s.setItems(Set.of(ItemMapper.toItemDto(item)));
            }
        });
        assertThat(requests.size(), equalTo(2));
        assertThat(requests, equalTo(expectedRequests));
    }

    @Test
    public void test4_getRequestById() {
        item.setRequest(itemRequest1);
        userService.createUser(user1);
        userService.createUser(user2);
        service.createRequest(1, itemRequest1);
        itemService.createItem(ItemMapper.toItemDto(item), 2L);
        ItemRequestDto newItemRequest1 = service.getRequestById(1L, 1L);
        assertThat(newItemRequest1.getId(), equalTo(1L));
        assertThat(newItemRequest1.getItems(), equalTo(Set.of(ItemMapper.toItemDto(item))));
        assertThat(newItemRequest1.getCreated(), equalTo(itemRequest1.getCreated()));
        assertThat(newItemRequest1.getDescription(), equalTo("Test request 1"));
    }
}