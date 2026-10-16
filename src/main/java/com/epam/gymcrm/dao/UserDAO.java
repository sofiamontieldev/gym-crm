package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.User;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDAO {

    private final SessionFactory sessionFactory;

    public UserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<User> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "from User user where user.username = :username",
                        User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select count(user) from User user where user.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }
}
