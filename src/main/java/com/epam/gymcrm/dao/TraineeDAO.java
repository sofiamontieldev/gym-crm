package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDAO {

    private final SessionFactory sessionFactory;

    public TraineeDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        if (trainee.getId() == null) {
            session.persist(trainee);
        } else {
            session.merge(trainee);
        }
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().find(Trainee.class, id));
    }

    public List<Trainee> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Trainee order by id", Trainee.class)
                .getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(sessionFactory.getCurrentSession()::remove);
    }
}
