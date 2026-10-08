package com.epam.gymcrm.dao;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class UserDAOTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private UserDAO userDAO;

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
                .setActive(true)
                .build();
    }
}
