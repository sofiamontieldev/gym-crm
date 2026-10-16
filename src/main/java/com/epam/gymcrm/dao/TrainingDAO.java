package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.SelectionQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAO {

    private static final String FETCH_GRAPH = """
            select training from Training training
            join fetch training.trainee trainee
            join fetch trainee.user traineeUser
            join fetch training.trainer trainer
            join fetch trainer.user trainerUser
            join fetch trainer.specialization specialization
            join fetch training.trainingType trainingType
            """;

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
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        FETCH_GRAPH + "where training.id = :id",
                        Training.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public List<Training> findAll() {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        FETCH_GRAPH + "order by training.trainingDate, training.id",
                        Training.class)
                .getResultList();
    }

    public List<Training> findByTraineeCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName) {

        StringBuilder hql = new StringBuilder(FETCH_GRAPH)
                .append("where traineeUser.username = :traineeUsername ");

        if (fromDate != null) {
            hql.append("and training.trainingDate >= :fromDate ");
        }
        if (toDate != null) {
            hql.append("and training.trainingDate <= :toDate ");
        }
        if (hasText(trainerUsername)) {
            hql.append("and trainerUser.username = :trainerUsername ");
        }
        if (hasText(trainingTypeName)) {
            hql.append("and trainingType.name = :trainingTypeName ");
        }
        hql.append("order by training.trainingDate, training.id");

        SelectionQuery<Training> query = sessionFactory.getCurrentSession()
                .createSelectionQuery(hql.toString(), Training.class)
                .setParameter("traineeUsername", traineeUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (hasText(trainerUsername)) {
            query.setParameter("trainerUsername", trainerUsername);
        }
        if (hasText(trainingTypeName)) {
            query.setParameter("trainingTypeName", trainingTypeName);
        }

        return query.getResultList();
    }

    public List<Training> findByTrainerCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        StringBuilder hql = new StringBuilder(FETCH_GRAPH)
                .append("where trainerUser.username = :trainerUsername ");

        if (fromDate != null) {
            hql.append("and training.trainingDate >= :fromDate ");
        }
        if (toDate != null) {
            hql.append("and training.trainingDate <= :toDate ");
        }
        if (hasText(traineeUsername)) {
            hql.append("and traineeUser.username = :traineeUsername ");
        }
        hql.append("order by training.trainingDate, training.id");

        SelectionQuery<Training> query = sessionFactory.getCurrentSession()
                .createSelectionQuery(hql.toString(), Training.class)
                .setParameter("trainerUsername", trainerUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (hasText(traineeUsername)) {
            query.setParameter("traineeUsername", traineeUsername);
        }

        return query.getResultList();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
