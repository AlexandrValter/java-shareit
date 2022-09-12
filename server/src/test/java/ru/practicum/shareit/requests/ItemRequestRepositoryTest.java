package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = ShareItApp.class)
public class ItemRequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRequestRepository repository;
    private final ItemRequest itemRequest1 = new ItemRequest();
    private final User user = new User();

    @BeforeEach
    public void preparation() {
        itemRequest1.setDescription("test1");
        itemRequest1.setCreated(LocalDateTime.now());
        user.setName("Test name");
        user.setEmail("test@test.ru");
        itemRequest1.setRequestor(user);
        entityManager.persist(user);
        entityManager.persist(itemRequest1);
        entityManager.flush();
    }

    @Test
    public void test1_tryFindAllItemRequestsByRequestorId() {
        List<ItemRequest> found = repository.findAllByRequestorId(1L);
        Assertions.assertEquals(1L, found.get(0).getId());
        Assertions.assertEquals(itemRequest1.getDescription(), found.get(0).getDescription());
        Assertions.assertEquals(itemRequest1.getCreated(), found.get(0).getCreated());
        Assertions.assertEquals(itemRequest1.getRequestor(), user);
    }

    @Test
    public void test2_tryFindAllItemRequests() {
        Pageable pageable = PageRequest.of(0, 5);
        List<ItemRequest> found = repository.findAll(pageable).getContent();
        Assertions.assertEquals(1L, found.get(0).getId());
        Assertions.assertEquals(itemRequest1.getDescription(), found.get(0).getDescription());
        Assertions.assertEquals(itemRequest1.getCreated(), found.get(0).getCreated());
        Assertions.assertEquals(itemRequest1.getRequestor(), user);
    }
}