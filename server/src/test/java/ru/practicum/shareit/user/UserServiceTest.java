package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItApp;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItApp.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final EntityManager em;
    private final UserService userService;
    private final User user1 = new User();
    private final User newUser = new User();

    @BeforeEach
    public void restartIdentity() {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE;").executeUpdate();
        em.createNativeQuery("TRUNCATE table users restart identity;").executeUpdate();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE;").executeUpdate();
        user1.setName("Test1");
        user1.setEmail("test1@ya.ru");
    }

    @Test
    public void test1_tryCreateUser() {
        userService.createUser(user1);
        TypedQuery<User> query = em.createQuery(
                "select u from User u where u.email = :email",
                User.class);
        User newUser = query.setParameter("email", user1.getEmail()).getSingleResult();
        assertThat(newUser.getId(), equalTo(1L));
        assertThat(newUser.getName(), equalTo(user1.getName()));
        assertThat(newUser.getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    public void test2_tryUpdateUser() {
        userService.createUser(user1);
        newUser.setName("New name");
        newUser.setEmail("email@newemail.com");
        userService.updateUser(1L, newUser);
        TypedQuery<User> query = em.createQuery(
                "select u from User u where u.id = :id",
                User.class);
        User updateUser = query.setParameter("id", user1.getId()).getSingleResult();
        assertThat(updateUser.getId(), equalTo(1L));
        assertThat(updateUser.getName(), equalTo(newUser.getName()));
        assertThat(updateUser.getEmail(), equalTo(newUser.getEmail()));
    }

    @Test
    public void test3_tryGetUserById() {
        userService.createUser(user1);
        User expectedUser = userService.getUser(user1.getId());
        assertThat(expectedUser, equalTo(user1));
    }

    @Test
    public void test4_tryGetAllUsers() {
        userService.createUser(user1);
        newUser.setName("user2");
        newUser.setEmail("test2@ya.ru");
        userService.createUser(newUser);
        List<User> userList = userService.getAllUsers();
        assertThat(userList, equalTo(List.of(user1, newUser)));
    }

    @Test
    public void test5_tryDeleteUser() {
        userService.createUser(user1);
        newUser.setName("user2");
        newUser.setEmail("test2@ya.ru");
        userService.createUser(newUser);
        userService.deleteUser(2L);
        List<User> userList = userService.getAllUsers();
        assertThat(userList, equalTo(List.of(user1)));

    }
}