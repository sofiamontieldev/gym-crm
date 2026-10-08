package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAO {

    private final SessionFactory sessionFactory;

    public TrainingDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Training training) {
        Session session = sessionFactory.getCurrentSession();
        if (training.getId() == null) {
            session.persist(training);
        } else {
            session.merge(training);
        }
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().find(Training.class, id));
    }

    public List<Training> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Training order by id", Training.class)
                .getResultList();
    }
}
