package com.epam.gymcrm.model;

import com.epam.gymcrm.config.AppConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class HibernatePersistenceTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void savesAndLoadsUser() {
        Session session = sessionFactory.getCurrentSession();
        User user = user("Alice", "Stone", "Alice.Stone");

        session.persist(user);
        session.flush();
        Long id = user.getId();
        session.clear();

        User saved = session.find(User.class, id);
        assertNotNull(saved);
        assertEquals("Alice.Stone", saved.getUsername());
        assertEquals("Alice", saved.getFirstName());
    }

    @Test
    void savesTraineeWithUserAndNullableOptionalFields() {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = new Trainee(
                user("Laura", "Diaz", "Laura.Diaz"),
                null,
                null);

        session.persist(trainee);
        session.flush();
        Long id = trainee.getId();
        session.clear();

        Trainee saved = session.find(Trainee.class, id);
        assertNotNull(saved.getUser().getId());
        assertEquals("Laura.Diaz", saved.getUsername());
        assertNull(saved.getDateOfBirth());
        assertNull(saved.getAddress());
    }

    @Test
    void savesTrainerWithUserAndTrainingType() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType yoga = new TrainingType("YOGA");
        session.persist(yoga);
        Trainer trainer = new Trainer(
                user("Mario", "Ruiz", "Mario.Ruiz"),
                yoga);

        session.persist(trainer);
        session.flush();
        Long id = trainer.getId();
        session.clear();

        Trainer saved = session.find(Trainer.class, id);
        assertEquals("Mario.Ruiz", saved.getUsername());
        assertEquals("YOGA", saved.getSpecialization().getName());
    }

    @Test
    void savesTrainingWithAllForeignKeys() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType fitness = new TrainingType("FITNESS");
        session.persist(fitness);

        Trainee trainee = new Trainee(
                user("Ana", "Lopez", "Ana.Lopez"),
                LocalDate.of(1995, 4, 12),
                "Main Street 10");
        Trainer trainer = new Trainer(
                user("Carlos", "Perez", "Carlos.Perez"),
                fitness);
        session.persist(trainee);
        session.persist(trainer);

        Training training = new Training(
                trainee,
                trainer,
                "Morning Fitness",
                fitness,
                LocalDate.of(2026, 10, 5),
                60);
        session.persist(training);
        session.flush();
        Long id = training.getId();
        session.clear();

        Training saved = session.find(Training.class, id);
        assertEquals("Morning Fitness", saved.getTrainingName());
        assertEquals(trainee.getId(), saved.getTraineeId());
        assertEquals(trainer.getId(), saved.getTrainerId());
        assertEquals("FITNESS", saved.getTrainingType().getName());
    }

    @Test
    void savesTraineeTrainerAssignmentOnlyOnce() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType resistance = new TrainingType("RESISTANCE");
        session.persist(resistance);
        Trainer trainer = new Trainer(
                user("Diana", "Mora", "Diana.Mora"),
                resistance);
        Trainee trainee = new Trainee(
                user("Luis", "Vega", "Luis.Vega"),
                LocalDate.of(1990, 2, 3),
                "North Avenue 20");

        trainee.assignTrainer(trainer);
        trainee.assignTrainer(trainer);
        session.persist(trainer);
        session.persist(trainee);
        session.flush();
        Long traineeId = trainee.getId();
        session.clear();

        Trainee saved = session.find(Trainee.class, traineeId);
        assertEquals(1, saved.getTrainers().size());
        assertSame(saved, saved.getTrainers().iterator().next().getTrainees().iterator().next());
    }

    private static User user(String firstName, String lastName, String username) {
        return new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
    }
}
