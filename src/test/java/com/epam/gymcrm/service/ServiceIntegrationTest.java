package com.epam.gymcrm.service;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymCrmFacade;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServiceIntegrationTest {

    @Autowired
    private GymCrmFacade facade;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private TrainerDAO trainerDAO;

    @Autowired
    private TrainingDAO trainingDAO;

    @Autowired
    private UserDAO userDAO;

    @Test
    void generatesPersistentUsernamesAndReturnsInitializedTraineeProfile() {
        Trainee trainee = facade.createTrainee(
                "Valentina",
                "Rojas",
                LocalDate.of(1995, 4, 12),
                "Cra. 43A # 10-20");
        Trainer repeatedName = facade.createTrainer("Valentina", "Rojas", "FITNESS");
        Trainer assigned = facade.createTrainer("Andres", "Gomez", "YOGA");

        assertEquals("Valentina.Rojas", trainee.getUsername());
        assertEquals("Valentina.Rojas1", repeatedName.getUsername());

        facade.replaceTrainers(
                trainee.getUsername(),
                trainee.getPassword(),
                List.of(assigned.getUsername()));

        Trainee profile = facade.selectTrainee(
                trainee.getUsername(),
                trainee.getPassword());

        assertEquals(
                List.of("Andres.Gomez"),
                profile.getTrainers().stream().map(Trainer::getUsername).toList());
        assertEquals(
                "YOGA",
                profile.getTrainers().iterator().next().getSpecialization().getName());
    }

    @Test
    void persistsPasswordProfileAndActivationChanges() {
        Trainee trainee = facade.createTrainee(
                "Camila",
                "Restrepo",
                LocalDate.of(1994, 8, 6),
                "Laureles");
        String username = trainee.getUsername();
        String firstPassword = trainee.getPassword();

        facade.changeTraineePassword(username, firstPassword, "NewPass123");
        assertThrows(
                AuthenticationException.class,
                () -> facade.authenticateTrainee(username, firstPassword));

        Trainee updated = facade.updateTrainee(
                username,
                "NewPass123",
                "Camila",
                "Montoya",
                LocalDate.of(1994, 8, 6),
                "Cl. 33 # 74-15");
        assertEquals("Montoya", updated.getLastName());
        assertEquals(username, updated.getUsername());

        facade.deactivateTrainee(username, "NewPass123");
        assertThrows(
                AuthenticationException.class,
                () -> facade.authenticateTrainee(username, "NewPass123"));
        assertThrows(
                IllegalStateException.class,
                () -> facade.deactivateTrainee(username, "NewPass123"));

        facade.activateTrainee(username, "NewPass123");
        assertTrue(facade.authenticateTrainee(username, "NewPass123").isActive());
    }

    @Test
    void rollsBackTrainerUpdateWhenTransactionIsMarkedForRollback() {
        Trainer trainer = facade.createTrainer("Santiago", "Martinez", "FITNESS");
        String username = trainer.getUsername();
        String password = trainer.getPassword();

        TransactionTemplate transactions = new TransactionTemplate(transactionManager);
        transactions.executeWithoutResult(status -> {
            Trainer updated = facade.updateTrainer(
                    username,
                    password,
                    "Santiago",
                    "Restrepo",
                    "YOGA");

            assertEquals("Restrepo", updated.getLastName());
            assertEquals("YOGA", updated.getSpecialization().getName());
            status.setRollbackOnly();
        });

        Trainer persisted = facade.selectTrainer(username, password);
        assertEquals("Martinez", persisted.getLastName());
        assertEquals("FITNESS", persisted.getSpecialization().getName());
    }

    @Test
    void hardDeletesTraineeDataAndPreservesTrainer() {
        Trainee trainee = facade.createTrainee(
                "Daniela",
                "Ospina",
                LocalDate.of(1993, 2, 15),
                "Cl. 10 # 32-15");
        Trainer trainer = facade.createTrainer("Santiago", "Martinez", "FITNESS");

        facade.replaceTrainers(
                trainee.getUsername(),
                trainee.getPassword(),
                List.of(trainer.getUsername()));
        facade.createTraining(
                trainer.getUsername(),
                trainer.getPassword(),
                trainee.getUsername(),
                trainer.getUsername(),
                "Functional Strength Training",
                "FITNESS",
                LocalDate.of(2026, 10, 5),
                60);

        facade.deleteTrainee(trainee.getUsername(), trainee.getPassword());

        TransactionTemplate transactions = new TransactionTemplate(transactionManager);
        transactions.executeWithoutResult(status -> {
            assertTrue(traineeDAO.findByUsername(trainee.getUsername()).isEmpty());
            assertTrue(userDAO.findByUsername(trainee.getUsername()).isEmpty());

            Trainer preservedTrainer = trainerDAO.findByUsername(trainer.getUsername()).orElseThrow();
            assertTrue(preservedTrainer.getTrainees().isEmpty());
            assertTrue(trainingDAO.findByTrainerCriteria(
                    trainer.getUsername(), null, null, null).isEmpty());
        });
    }
}
