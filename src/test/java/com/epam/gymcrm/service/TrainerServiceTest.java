package com.epam.gymcrm.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingTypeDAO;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainerServiceTest {

    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private AuthenticationService authenticationService;
    private PasswordGenerator passwordGenerator;
    private UsernameGenerator usernameGenerator;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerDAO = mock(TrainerDAO.class);
        trainingTypeDAO = mock(TrainingTypeDAO.class);
        authenticationService = mock(AuthenticationService.class);
        passwordGenerator = mock(PasswordGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        trainerService = new TrainerService(
                trainerDAO,
                trainingTypeDAO,
                authenticationService,
                passwordGenerator,
                usernameGenerator);
    }

    @Test
    void createsTrainerWithPersistedSpecialization() {
        TrainingType fitness = new TrainingType("FITNESS");
        when(trainingTypeDAO.findByName("FITNESS")).thenReturn(Optional.of(fitness));
        when(usernameGenerator.generate("Andres", "Gomez")).thenReturn("Andres.Gomez");
        when(passwordGenerator.generate()).thenReturn("Abc1234567");

        Trainer trainer = trainerService.createTrainer("Andres", "Gomez", "FITNESS");

        assertEquals("Andres.Gomez", trainer.getUsername());
        assertEquals(10, trainer.getPassword().length());
        assertSame(fitness, trainer.getSpecialization());
        verify(trainerDAO).save(trainer);
    }

    @Test
    void selectsUpdatesAndChangesPasswordForAuthenticatedTrainer() {
        TrainingType fitness = new TrainingType("FITNESS");
        TrainingType yoga = new TrainingType("YOGA");
        Trainer trainer = trainer(fitness);
        when(authenticationService.authenticate("Andres.Gomez", "Abc1234567"))
                .thenReturn(trainer.getUser());
        when(trainerDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(trainer));
        when(trainingTypeDAO.findByName("YOGA")).thenReturn(Optional.of(yoga));

        assertSame(
                trainer,
                trainerService.selectTrainer("Andres.Gomez", "Abc1234567"));

        Trainer updated = trainerService.updateTrainer(
                "Andres.Gomez",
                "Abc1234567",
                "Andres",
                "Restrepo",
                "YOGA");
        trainerService.changeTrainerPassword(
                "Andres.Gomez",
                "Abc1234567",
                "NewPass123");

        assertEquals("Restrepo", updated.getLastName());
        assertEquals("Andres.Gomez", updated.getUsername());
        assertSame(yoga, updated.getSpecialization());
        assertEquals("NewPass123", updated.getPassword());
    }

    @Test
    void enforcesActivationTransitionsAndRejectsUnknownSpecialization() {
        Trainer trainer = trainer(new TrainingType("FITNESS"));
        when(authenticationService.verifyCredentials("Andres.Gomez", "Abc1234567"))
                .thenReturn(trainer.getUser());
        when(trainerDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(trainer));
        when(trainingTypeDAO.findByName("UNKNOWN")).thenReturn(Optional.empty());

        trainerService.deactivateTrainer("Andres.Gomez", "Abc1234567");
        assertThrows(
                IllegalStateException.class,
                () -> trainerService.deactivateTrainer("Andres.Gomez", "Abc1234567"));
        trainerService.activateTrainer("Andres.Gomez", "Abc1234567");
        assertTrue(trainer.isActive());

        Logger logger = (Logger) LoggerFactory.getLogger(TrainerService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> trainerService.createTrainer("Camila", "Restrepo", "UNKNOWN"));
        } finally {
            logger.detachAppender(appender);
        }

        assertEquals(1, appender.list.size());
        assertEquals(Level.WARN, appender.list.getFirst().getLevel());
        assertTrue(appender.list.getFirst().getFormattedMessage().contains("UNKNOWN"));
    }

    private static Trainer trainer(TrainingType specialization) {
        User user = new User.Builder()
                .setFirstName("Andres")
                .setLastName("Gomez")
                .setUsername("Andres.Gomez")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
        return new Trainer(user, specialization);
    }
}
