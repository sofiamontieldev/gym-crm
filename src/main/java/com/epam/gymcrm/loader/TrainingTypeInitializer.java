package com.epam.gymcrm.loader;

import com.epam.gymcrm.model.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class TrainingTypeInitializer {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeInitializer.class);

    private final SessionFactory sessionFactory;

    public TrainingTypeInitializer(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initializeTrainingTypes() {
        Session session = sessionFactory.getCurrentSession();
        Set<String> existingNames = new HashSet<>(session
                .createSelectionQuery(
                        "select trainingType.name from TrainingType trainingType",
                        String.class)
                .getResultList());

        int insertedCount = 0;
        for (String name : TrainingType.FIXED_NAMES) {
            if (existingNames.add(name)) {
                session.persist(new TrainingType(name));
                insertedCount++;
            }
        }

        log.info("Training type initialization completed: {} inserted", insertedCount);
    }
}
