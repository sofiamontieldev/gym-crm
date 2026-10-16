package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDAO {

    private final SessionFactory sessionFactory;

    public TrainingTypeDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<TrainingType> findByName(String name) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "from TrainingType trainingType where trainingType.name = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .uniqueResultOptional();
    }

    public List<TrainingType> findAll() {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "from TrainingType trainingType order by trainingType.name",
                        TrainingType.class)
                .getResultList();
    }
}
