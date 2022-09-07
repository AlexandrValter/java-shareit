package ru.practicum.shareit.item;

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
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRepository repository;
    private final User user1 = new User();
    private final User user2 = new User();
    private final Item item1 = new Item();
    private final Item item2 = new Item();
    private final ItemRequest itemRequest = new ItemRequest();

    @BeforeEach
    public void preparation() {
        user1.setName("User1");
        user1.setEmail("user1@ya.ru");
        user2.setName("User2");
        user2.setEmail("user2@ya.ru");
        item1.setOwner(user1);
        item1.setName("Item1");
        item1.setAvailable(true);
        item1.setDescription("Item for test 1");
        item2.setOwner(user2);
        item2.setName("Item2");
        item2.setAvailable(true);
        item2.setDescription("Item for test 2");
        itemRequest.setRequestor(user1);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setDescription("Item request for test");
        item2.setRequest(itemRequest);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(itemRequest);
        entityManager.flush();
    }

    @Test
    public void test1_tryFindAllByOwnerId() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Item> item = repository.findAllByOwnerId(user1.getId(), pageable).getContent();
        Assertions.assertEquals(1L, item.get(0).getId());
    }

    @Test
    public void test2_trySearchItems() {
        Query nativeQuery = entityManager.getEntityManager()
                .createNativeQuery("SELECT * from items " +
                        "where lower(name) like lower(concat('%', :text, '%')) " +
                        "or lower(description) like lower(concat('%', :text, '%')) " +
                        "and available=true", Item.class);
        nativeQuery.setParameter("text", "item for test 1");
        List<Item> items = nativeQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(item1, items.get(0));
    }

    @Test
    public void test3_tryFindItemsByRequest() {
        TypedQuery<Item> query = entityManager.getEntityManager()
                .createQuery("Select i from Item i where i.request.id = :id", Item.class);
        List<Item> item = query.setParameter("id", itemRequest.getId()).getResultList();
        Assertions.assertEquals(1, item.size());
        Assertions.assertEquals(item2, item.get(0));
    }
}