package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAO {

    private final SessionFactory sessionFactory;

    public TrainerDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();
        if (trainer.getId() == null) {
            session.persist(trainer);
        } else {
            session.merge(trainer);
        }
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().find(Trainer.class, id));
    }

    public List<Trainer> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainer order by id", Trainer.class)
                .getResultList();
    }
}
