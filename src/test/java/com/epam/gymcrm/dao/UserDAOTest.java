package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.User;
import com.epam.gymcrm.model.UserRole;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserDAOTest {

    private final SessionFactory sessionFactory;
    private final UserDAO userDAO;

    @Autowired
    UserDAOTest(SessionFactory sessionFactory, UserDAO userDAO) {
        this.sessionFactory = sessionFactory;
        this.userDAO = userDAO;
    }

    @Test
    void findsExistingUserByUsername() {
        User user = user("Alicia", "Torres", "Alicia.Torres");
        sessionFactory.getCurrentSession().persist(user);

        User found = userDAO.findByUsername("Alicia.Torres").orElseThrow();

        assertEquals(user.getId(), found.getId());
        assertTrue(userDAO.existsByUsername("Alicia.Torres"));
    }

    @Test
    void returnsEmptyForUnknownUsername() {
        assertTrue(userDAO.findByUsername("Usuario.Inexistente").isEmpty());
        assertFalse(userDAO.existsByUsername("Usuario.Inexistente"));
    }

    private static User user(String firstName, String lastName, String username) {
        return new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword("Abc1234567")
                .setRole(UserRole.TRAINEE)
                .setActive(true)
                .build();
    }
}
