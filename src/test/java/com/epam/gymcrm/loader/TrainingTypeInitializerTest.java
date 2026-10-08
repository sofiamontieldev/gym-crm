package com.epam.gymcrm.loader;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(AppConfig.class)
class TrainingTypeInitializerTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TrainingTypeInitializer initializer;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void loadsTheFiveFixedTrainingTypesAtStartup() {
        List<String> names = trainingTypeNames();

        assertEquals(5, names.size());
        assertEquals(TrainingType.FIXED_NAMES, Set.copyOf(names));
    }

    @Test
    void doesNotDuplicateTrainingTypesWhenInitializationRunsAgain() {
        initializer.initializeTrainingTypes();
        initializer.initializeTrainingTypes();

        List<String> names = trainingTypeNames();
        assertEquals(5, names.size());
        assertEquals(TrainingType.FIXED_NAMES, Set.copyOf(names));
    }

    @Test
    void doesNotCreateDemoProfilesOrTrainings() {
        transactionTemplate.executeWithoutResult(status -> {
            Session session = sessionFactory.getCurrentSession();

            assertEquals(0L, count(session, "User"));
            assertEquals(0L, count(session, "Trainee"));
            assertEquals(0L, count(session, "Trainer"));
            assertEquals(0L, count(session, "Training"));
        });
    }

    private List<String> trainingTypeNames() {
        return transactionTemplate.execute(status -> sessionFactory
                .getCurrentSession()
                .createSelectionQuery(
                        "select trainingType.name from TrainingType trainingType",
                        String.class)
                .getResultList());
    }

    private static long count(Session session, String entityName) {
        return session.createSelectionQuery(
                        "select count(entity) from " + entityName + " entity",
                        Long.class)
                .getSingleResult();
    }
}
