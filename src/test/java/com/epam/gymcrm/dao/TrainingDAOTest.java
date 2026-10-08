package com.epam.gymcrm.dao;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainingDAOTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TrainingDAO trainingDAO;

    @Autowired
    private TrainingTypeDAO trainingTypeDAO;

    private Training firstTraining;

    @BeforeEach
    void setUp() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType fitness = trainingTypeDAO.findByName("FITNESS").orElseThrow();
        TrainingType yoga = trainingTypeDAO.findByName("YOGA").orElseThrow();

        Trainee valentina = trainee("Valentina", "Rojas", "Valentina.Rojas");
        Trainee camila = trainee("Camila", "Rodriguez", "Camila.Rodriguez");
        Trainer andres = trainer("Andres", "Gomez", "Andres.Gomez", fitness);
        Trainer camilo = trainer("Camilo", "Restrepo", "Camilo.Restrepo", yoga);

        session.persist(valentina);
        session.persist(camila);
        session.persist(andres);
        session.persist(camilo);

        firstTraining = new Training(
                valentina,
                andres,
                "Functional Strength Training",
                fitness,
                LocalDate.of(2026, 1, 10),
                60);
        Training secondTraining = new Training(
                valentina,
                camilo,
                "Morning Yoga",
                yoga,
                LocalDate.of(2026, 1, 20),
                45);
        Training thirdTraining = new Training(
                valentina,
                andres,
                "Afternoon Cardio",
                fitness,
                LocalDate.of(2026, 2, 10),
                50);
        Training otherTraineeTraining = new Training(
                camila,
                andres,
                "Guided Resistance Training",
                fitness,
                LocalDate.of(2026, 1, 20),
                40);

        trainingDAO.save(firstTraining);
        trainingDAO.save(secondTraining);
        trainingDAO.save(thirdTraining);
        trainingDAO.save(otherTraineeTraining);
        session.flush();
    }

    @Test
    void findsTrainingWithRequiredAssociationsInitialized() {
        Long trainingId = firstTraining.getId();
        sessionFactory.getCurrentSession().clear();

        Training found = trainingDAO.findById(trainingId).orElseThrow();

        assertEquals("Functional Strength Training", found.getTrainingName());
        assertTrue(Hibernate.isInitialized(found.getTrainee()));
        assertTrue(Hibernate.isInitialized(found.getTrainee().getUser()));
        assertTrue(Hibernate.isInitialized(found.getTrainer()));
        assertTrue(Hibernate.isInitialized(found.getTrainer().getUser()));
        assertTrue(Hibernate.isInitialized(found.getTrainer().getSpecialization()));
        assertTrue(Hibernate.isInitialized(found.getTrainingType()));
    }

    @Test
    void filtersTraineeTrainingsWithInclusiveDatesAndOptionalCriteria() {
        List<Training> allTrainings = trainingDAO.findByTraineeCriteria(
                "Valentina.Rojas", null, null, " ", null);
        List<Training> januaryTrainings = trainingDAO.findByTraineeCriteria(
                "Valentina.Rojas",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20),
                null,
                null);
        List<Training> filtered = trainingDAO.findByTraineeCriteria(
                "Valentina.Rojas",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20),
                "Andres.Gomez",
                "FITNESS");

        assertEquals(
                List.of("Functional Strength Training", "Morning Yoga", "Afternoon Cardio"),
                names(allTrainings));
        assertEquals(List.of("Functional Strength Training", "Morning Yoga"), names(januaryTrainings));
        assertEquals(List.of("Functional Strength Training"), names(filtered));
        assertTrue(trainingDAO.findByTraineeCriteria(
                "Valentina.Rojas", null, null, null, "ZUMBA").isEmpty());
    }

    @Test
    void filtersTrainerTrainingsByPeriodAndTraineeUsername() {
        List<Training> filtered = trainingDAO.findByTrainerCriteria(
                "Andres.Gomez",
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 20),
                "Camila.Rodriguez");

        assertEquals(List.of("Guided Resistance Training"), names(filtered));
        assertTrue(trainingDAO.findByTrainerCriteria(
                "Entrenador.Inexistente", null, null, null).isEmpty());
    }

    private static List<String> names(List<Training> trainings) {
        return trainings.stream()
                .map(Training::getTrainingName)
                .toList();
    }

    private static Trainee trainee(String firstName, String lastName, String username) {
        return new Trainee(
                user(firstName, lastName, username),
                LocalDate.of(1995, 4, 12),
                "Calle 10 # 20-30");
    }

    private static Trainer trainer(
            String firstName,
            String lastName,
            String username,
            TrainingType specialization) {

        return new Trainer(user(firstName, lastName, username), specialization);
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
