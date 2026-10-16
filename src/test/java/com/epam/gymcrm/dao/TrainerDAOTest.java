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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainerDAOTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TrainerDAO trainerDAO;

    @Autowired
    private TrainingTypeDAO trainingTypeDAO;

    @Test
    void findsTrainerByUsernameAndUsernameCollection() {
        TrainingType fitness = trainingTypeDAO.findByName("FITNESS").orElseThrow();
        Trainer trainer = trainer("Andres", "Gomez", "Andres.Gomez", fitness, true);
        trainerDAO.save(trainer);

        assertEquals(
                "FITNESS",
                trainerDAO.findByUsername("Andres.Gomez")
                        .orElseThrow()
                        .getSpecialization()
                        .getName());
        assertEquals(
                List.of("Andres.Gomez"),
                trainerDAO.findAllByUsernames(List.of("Andres.Gomez", "Andres.Gomez"))
                        .stream()
                        .map(Trainer::getUsername)
                        .toList());
        assertTrue(trainerDAO.findAllByUsernames(List.of()).isEmpty());
    }

    @Test
    void findsOnlyActiveTrainersNotAssignedToTrainee() {
        Session session = sessionFactory.getCurrentSession();
        TrainingType fitness = trainingTypeDAO.findByName("FITNESS").orElseThrow();
        Trainer assigned = trainer("Andres", "Gomez", "Andres.Gomez", fitness, true);
        Trainer available = trainer("Camilo", "Restrepo", "Camilo.Restrepo", fitness, true);
        Trainer inactive = trainer("Daniela", "Ospina", "Daniela.Ospina", fitness, false);
        Trainee trainee = new Trainee(
                user("Valentina", "Rojas", "Valentina.Rojas", true),
                LocalDate.of(1995, 4, 12),
                "Carrera 7 # 45-12");

        session.persist(assigned);
        session.persist(available);
        session.persist(inactive);
        trainee.assignTrainer(assigned);
        session.persist(trainee);
        session.flush();

        List<String> usernames = trainerDAO.findActiveNotAssignedTo(trainee).stream()
                .map(Trainer::getUsername)
                .toList();

        assertEquals(List.of("Camilo.Restrepo"), usernames);
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
