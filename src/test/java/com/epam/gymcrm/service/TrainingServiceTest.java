package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.dao.TrainingTypeDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingServiceTest {

    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private AuthenticationService authenticationService;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingDAO = mock(TrainingDAO.class);
        traineeDAO = mock(TraineeDAO.class);
        trainerDAO = mock(TrainerDAO.class);
        trainingTypeDAO = mock(TrainingTypeDAO.class);
        authenticationService = mock(AuthenticationService.class);
        trainingService = new TrainingService(
                trainingDAO,
                traineeDAO,
                trainerDAO,
                trainingTypeDAO,
                authenticationService);
    }

    @Test
    void createsTrainingWithPersistentRelationsAndAuthenticatedUser() {
        Trainee trainee = trainee();
        Trainer trainer = trainer();
        TrainingType fitness = trainer.getSpecialization();
        when(authenticationService.authenticate("Camila.Restrepo", "Abc1234567"))
                .thenReturn(user("Camila", "Restrepo", "Camila.Restrepo"));
        when(traineeDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(trainer));
        when(trainingTypeDAO.findByName("FITNESS")).thenReturn(Optional.of(fitness));

        Training training = trainingService.createTraining(
                "Camila.Restrepo",
                "Abc1234567",
                "Valentina.Rojas",
                "Andres.Gomez",
                "Morning Fitness",
                "FITNESS",
                LocalDate.of(2026, 10, 5),
                60);

        assertSame(trainee, training.getTrainee());
        assertSame(trainer, training.getTrainer());
        assertSame(fitness, training.getTrainingType());
        assertEquals("Morning Fitness", training.getTrainingName());
        verify(trainingDAO).save(training);
    }

    @Test
    void delegatesAuthenticatedTraineeAndTrainerTrainingSearches() {
        Trainee trainee = trainee();
        Trainer trainer = trainer();
        Training training = new Training(
                trainee,
                trainer,
                "Functional Strength Training",
                trainer.getSpecialization(),
                LocalDate.of(2026, 10, 5),
                50);
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(authenticationService.authenticate("Andres.Gomez", "Abc1234567"))
                .thenReturn(trainer.getUser());
        when(traineeDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(trainer));
        when(trainingDAO.findByTraineeCriteria(
                "Valentina.Rojas", null, null, "Andres.Gomez", "FITNESS"))
                .thenReturn(List.of(training));
        when(trainingDAO.findByTrainerCriteria(
                "Andres.Gomez", null, null, "Valentina.Rojas"))
                .thenReturn(List.of(training));

        assertEquals(
                List.of(training),
                trainingService.selectTraineeTrainings(
                        "Valentina.Rojas",
                        "Abc1234567",
                        null,
                        null,
                        "Andres.Gomez",
                        "FITNESS"));
        assertEquals(
                List.of(training),
                trainingService.selectTrainerTrainings(
                        "Andres.Gomez",
                        "Abc1234567",
                        null,
                        null,
                        "Valentina.Rojas"));
    }

    @Test
    void rejectsInvalidDateRangeAndMissingParticipant() {
        Trainee trainee = trainee();
        when(authenticationService.authenticate("Valentina.Rojas", "Abc1234567"))
                .thenReturn(trainee.getUser());
        when(traineeDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(trainee));
        when(traineeDAO.findByUsername("Persona.Inexistente")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.selectTraineeTrainings(
                        "Valentina.Rojas",
                        "Abc1234567",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 5),
                        null,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.createTraining(
                        "Valentina.Rojas",
                        "Abc1234567",
                        "Persona.Inexistente",
                        "Andres.Gomez",
                        "Morning Fitness",
                        "FITNESS",
                        LocalDate.of(2026, 10, 5),
                        60));
    }

    private static Trainee trainee() {
        return new Trainee(
                user("Valentina", "Rojas", "Valentina.Rojas"),
                LocalDate.of(1995, 4, 12),
                "Cra. 43A # 10-20");
    }

    private static Trainer trainer() {
        return new Trainer(
                user("Andres", "Gomez", "Andres.Gomez"),
                new TrainingType("FITNESS"));
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
