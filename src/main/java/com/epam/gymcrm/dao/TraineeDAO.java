package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainee from Trainee trainee " +
                                "join fetch trainee.user " +
                                "where trainee.id = :id",
                        Trainee.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public Optional<Trainee> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainee from Trainee trainee " +
                                "join fetch trainee.user user " +
                                "where user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public Optional<Trainee> findProfileByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select distinct trainee from Trainee trainee " +
                                "join fetch trainee.user user " +
                                "left join fetch trainee.trainers trainer " +
                                "left join fetch trainer.user " +
                                "left join fetch trainer.specialization " +
                                "where user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainee> findAll() {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainee from Trainee trainee " +
                                "join fetch trainee.user user " +
                                "order by user.lastName, user.firstName, trainee.id",
                        Trainee.class)
                .getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(sessionFactory.getCurrentSession()::remove);
    }

    public void delete(Trainee trainee) {
        Objects.requireNonNull(trainee, "trainee must not be null");
        sessionFactory.getCurrentSession().remove(trainee);
    }

    public void replaceTrainers(Trainee trainee, Collection<Trainer> trainers) {
        Objects.requireNonNull(trainee, "trainee must not be null");
        trainee.replaceTrainers(trainers);
    }
}
