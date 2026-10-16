package com.epam.gymcrm.dao;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TraineeDAOTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private TrainerDAO trainerDAO;

    @Autowired
    private TrainingTypeDAO trainingTypeDAO;

    @Test
    void savesAndFindsTraineeByUsername() {
        Trainee trainee = trainee("Valentina", "Rojas", "Valentina.Rojas");

        traineeDAO.save(trainee);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Trainee found = traineeDAO.findByUsername("Valentina.Rojas").orElseThrow();
        assertEquals("Valentina", found.getFirstName());
        assertTrue(traineeDAO.findByUsername("Persona.Inexistente").isEmpty());
    }

    @Test
    void replacesAndClearsTrainerAssignmentsWithoutDeletingTrainers() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType fitness = trainingTypeDAO.findByName("FITNESS").orElseThrow();
        TrainingType yoga = trainingTypeDAO.findByName("YOGA").orElseThrow();
        Trainer firstTrainer = trainer("Andres", "Gomez", "Andres.Gomez", fitness, true);
        Trainer replacement = trainer("Camilo", "Restrepo", "Camilo.Restrepo", yoga, true);
        Trainee trainee = trainee("Valentina", "Rojas", "Valentina.Rojas");

        session.persist(firstTrainer);
        session.persist(replacement);
        trainee.assignTrainer(firstTrainer);
        traineeDAO.save(trainee);
        session.flush();

        traineeDAO.replaceTrainers(trainee, List.of(replacement, replacement));
        session.flush();
        session.clear();

        Trainee updated = traineeDAO.findByUsername("Valentina.Rojas").orElseThrow();
        Set<String> assignedUsernames = updated.getTrainers().stream()
                .map(Trainer::getUsername)
                .collect(Collectors.toSet());

        assertEquals(Set.of("Camilo.Restrepo"), assignedUsernames);
        assertTrue(trainerDAO.findByUsername("Andres.Gomez").isPresent());
        assertTrue(trainerDAO.findByUsername("Camilo.Restrepo").isPresent());

        traineeDAO.replaceTrainers(updated, List.of());
        session.flush();
        session.clear();

        assertTrue(traineeDAO.findByUsername("Valentina.Rojas")
                .orElseThrow()
                .getTrainers()
                .isEmpty());
        assertTrue(trainerDAO.findByUsername("Andres.Gomez").isPresent());
        assertTrue(trainerDAO.findByUsername("Camilo.Restrepo").isPresent());
    }

    private static Trainee trainee(String firstName, String lastName, String username) {
        return new Trainee(
                user(firstName, lastName, username, true),
                LocalDate.of(1995, 4, 12),
                "Calle 10 # 20-30");
    }

    private static Trainer trainer(
            String firstName,
            String lastName,
            String username,
            TrainingType specialization,
            boolean active) {

        return new Trainer(user(firstName, lastName, username, active), specialization);
    }

    private static User user(
            String firstName,
            String lastName,
            String username,
            boolean active) {

        return new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword("Abc1234567")
                .setActive(active)
                .build();
    }
}
