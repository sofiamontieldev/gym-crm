package com.epam.gymcrm.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraineeServiceTest {

    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private AuthenticationService authenticationService;
    private PasswordGenerator passwordGenerator;
    private UsernameGenerator usernameGenerator;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(TraineeDAO.class);
        trainerDAO = mock(TrainerDAO.class);
        authenticationService = mock(AuthenticationService.class);
        passwordGenerator = mock(PasswordGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        traineeService = new TraineeService(
                traineeDAO,
                trainerDAO,
                authenticationService,
                passwordGenerator,
                usernameGenerator);
    }

    @Test
    void createsTraineeWithGeneratedCredentials() {
        when(usernameGenerator.generate("Valentina", "Rojas")).thenReturn("Valentina.Rojas");
        when(passwordGenerator.generate()).thenReturn("Abc1234567");

        Trainee trainee = traineeService.createTrainee(
                "Valentina",
                "Rojas",
                LocalDate.of(1995, 4, 12),
                "Cra. 43A # 10-20");

        assertEquals("Valentina.Rojas", trainee.getUsername());
        assertEquals(10, trainee.getPassword().length());
        assertTrue(trainee.isActive());
        verify(traineeDAO).save(trainee);
    }

    @Test
    void selectsProfileAndChangesPasswordWithValidCredentials() {
        Trainee trainee = trainee();
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(traineeDAO.findProfileByUsername("Valentina.Rojas"))
                .thenReturn(Optional.of(trainee));
        when(traineeDAO.findByUsername("Valentina.Rojas"))
                .thenReturn(Optional.of(trainee));

        assertSame(
                trainee,
                traineeService.selectTrainee("Valentina.Rojas", "Abc1234567"));

        Logger logger = (Logger) LoggerFactory.getLogger(TraineeService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            traineeService.changeTraineePassword(
                    "Valentina.Rojas",
                    "Abc1234567",
                    "NewPass123");
        } finally {
            logger.detachAppender(appender);
        }

        assertEquals("NewPass123", trainee.getPassword());
        assertTrue(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .noneMatch(message -> message.contains("Abc1234567")
                        || message.contains("NewPass123")));
    }

    @Test
    void updatesProfileAndEnforcesActivationTransitions() {
        Trainee trainee = trainee();
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(authenticationService.verifyCredentials("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(traineeDAO.findByUsername("Valentina.Rojas"))
                .thenReturn(Optional.of(trainee));

        Trainee updated = traineeService.updateTrainee(
                "Valentina.Rojas",
                "Abc1234567",
                "Valentina",
                "Montoya",
                LocalDate.of(1996, 5, 20),
                "Laureles");

        assertSame(trainee, updated);
        assertEquals("Montoya", updated.getLastName());
        assertEquals("Valentina.Rojas", updated.getUsername());
        assertEquals("Laureles", updated.getAddress());

        traineeService.deactivateTrainee("Valentina.Rojas", "Abc1234567");
        assertThrows(
                IllegalStateException.class,
                () -> traineeService.deactivateTrainee("Valentina.Rojas", "Abc1234567"));
        traineeService.activateTrainee("Valentina.Rojas", "Abc1234567");
        assertTrue(trainee.isActive());
    }

    @Test
    void findsAvailableTrainersAndRejectsInactiveAssignmentsWithoutPartialUpdate() {
        Trainee trainee = trainee();
        Trainer active = trainer("Andres", "Gomez", "Andres.Gomez", true);
        Trainer inactive = trainer("Daniela", "Ospina", "Daniela.Ospina", false);
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(traineeDAO.findByUsername("Valentina.Rojas"))
                .thenReturn(Optional.of(trainee));
        when(trainerDAO.findActiveNotAssignedTo(trainee)).thenReturn(List.of(active));
        when(trainerDAO.findAllByUsernames(Set.of("Andres.Gomez")))
                .thenReturn(List.of(active));
        when(trainerDAO.findAllByUsernames(Set.of("Daniela.Ospina")))
                .thenReturn(List.of(inactive));

        assertEquals(
                List.of(active),
                traineeService.selectActiveUnassignedTrainers(
                        "Valentina.Rojas",
                        "Abc1234567"));

        traineeService.replaceTrainers(
                "Valentina.Rojas",
                "Abc1234567",
                List.of("Andres.Gomez", "Andres.Gomez"));
        assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.replaceTrainers(
                        "Valentina.Rojas",
                        "Abc1234567",
                        List.of("Daniela.Ospina")));

        verify(traineeDAO, times(1)).replaceTrainers(trainee, List.of(active));
    }

    @Test
    void deletesAuthenticatedTrainee() {
        Trainee trainee = trainee();
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(traineeDAO.findByUsername("Valentina.Rojas"))
                .thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("Valentina.Rojas", "Abc1234567");

        verify(traineeDAO).delete(trainee);
    }

    private static Trainee trainee() {
        return new Trainee(
                user("Valentina", "Rojas", "Valentina.Rojas", true),
                LocalDate.of(1995, 4, 12),
                "Cra. 43A # 10-20");
    }

    private static Trainer trainer(
            String firstName,
            String lastName,
            String username,
            boolean active) {

        return new Trainer(
                user(firstName, lastName, username, active),
                new TrainingType("FITNESS"));
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
