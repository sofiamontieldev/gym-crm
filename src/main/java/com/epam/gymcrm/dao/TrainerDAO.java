package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainer from Trainer trainer " +
                                "join fetch trainer.user " +
                                "join fetch trainer.specialization " +
                                "where trainer.id = :id",
                        Trainer.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public Optional<Trainer> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainer from Trainer trainer " +
                                "join fetch trainer.user user " +
                                "join fetch trainer.specialization " +
                                "where user.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainer> findAll() {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainer from Trainer trainer " +
                                "join fetch trainer.user user " +
                                "join fetch trainer.specialization " +
                                "order by user.lastName, user.firstName, trainer.id",
                        Trainer.class)
                .getResultList();
    }

    public List<Trainer> findAllByUsernames(Collection<String> usernames) {
        Objects.requireNonNull(usernames, "usernames must not be null");
        Set<String> uniqueUsernames = new LinkedHashSet<>(usernames);
        if (uniqueUsernames.isEmpty()) {
            return List.of();
        }

        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainer from Trainer trainer " +
                                "join fetch trainer.user user " +
                                "join fetch trainer.specialization " +
                                "where user.username in :usernames " +
                                "order by user.lastName, user.firstName, trainer.id",
                        Trainer.class)
                .setParameter("usernames", uniqueUsernames)
                .getResultList();
    }

    public List<Trainer> findActiveNotAssignedTo(Trainee trainee) {
        Objects.requireNonNull(trainee, "trainee must not be null");

        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select trainer from Trainer trainer " +
                                "join fetch trainer.user user " +
                                "join fetch trainer.specialization " +
                                "where user.active = true " +
                                "and trainer not in (" +
                                "select assignedTrainer from Trainee assignedTrainee " +
                                "join assignedTrainee.trainers assignedTrainer " +
                                "where assignedTrainee = :trainee) " +
                                "order by user.lastName, user.firstName, trainer.id",
                        Trainer.class)
                .setParameter("trainee", trainee)
                .getResultList();
    }
}
