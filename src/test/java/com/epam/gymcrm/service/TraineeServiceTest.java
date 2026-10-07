package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeServiceTest {

    private Map<Long, Trainee> traineeStorage;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        traineeStorage = new HashMap<>();
        Map<Long, Trainer> trainerStorage = new HashMap<>();
        UsernameGenerator usernameGenerator = new UsernameGenerator();

        traineeService = new TraineeService(
                new TraineeDAO(traineeStorage),
                new PasswordGenerator(),
                usernameGenerator);
        trainerService = new TrainerService(
                new TrainerDAO(trainerStorage),
                new PasswordGenerator(),
                usernameGenerator);
    }

    @Test
    void shouldCreateTraineeWithGeneratedCredentials() {
        Trainee trainee = traineeService.createTrainee(
                "Pablo",
                "Ordoñez",
                LocalDate.of(1995, 4, 12),
                "Main Street 123");

        assertEquals(1L, trainee.getId());
        assertEquals("Pablo.Ordoñez", trainee.getUsername());
        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());
        assertTrue(trainee.isActive());
        assertSame(trainee, traineeStorage.get(1L));
    }

    @Test
    void shouldGenerateUniqueUsernameAcrossTraineesAndTrainers() {
        Trainee trainee = traineeService.createTrainee(
                "Pablo", "Ordoñez", LocalDate.of(1995, 4, 12), "Address");
        Trainer trainer = trainerService.createTrainer(
                "Pablo", "Ordoñez", TrainingType.FITNESS);

        assertEquals("Pablo.Ordoñez", trainee.getUsername());
        assertEquals("Pablo.Smith1", trainer.getUsername());
    }

    @Test
    void shouldUpdateTraineeWithoutChangingCredentials() {
        Trainee trainee = traineeService.createTrainee(
                "Pablo", "Ordoñez", LocalDate.of(1995, 4, 12), "Old Address");
        String username = trainee.getUsername();
        String password = trainee.getPassword();

        Trainee updated = traineeService.updateTrainee(
                trainee.getId(),
                "Michael",
                "Brown",
                LocalDate.of(1990, 2, 10),
                "New Address");

        assertSame(trainee, updated);
        assertEquals("Michael", updated.getFirstName());
        assertEquals("Brown", updated.getLastName());
        assertEquals(LocalDate.of(1990, 2, 10), updated.getDateOfBirth());
        assertEquals("New Address", updated.getAddress());
        assertEquals(username, updated.getUsername());
        assertEquals(password, updated.getPassword());
    }

    @Test
    void shouldFailWhenUpdatingUnknownTrainee() {
        assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.updateTrainee(
                        999L,
                        "Pablo",
                        "Ordoñez",
                        LocalDate.of(1995, 4, 12),
                        "Address"));
    }

    @Test
    void shouldDeleteTrainee() {
        Trainee trainee = traineeService.createTrainee(
                "Pablo", "Ordoñez", LocalDate.of(1995, 4, 12), "Address");

        traineeService.deleteTrainee(trainee.getId());

        assertTrue(traineeService.selectTrainee(trainee.getId()).isEmpty());
    }

    private static Trainee createTrainee(Long id) {
        return new Trainee(
                new User.Builder()
                        .setId(id)
                        .setFirstName("Alicia")
                        .setLastName("Montoya")
                        .setUsername("Alicia.Montoya")
                        .setPassword("FirstPass1")
                        .setActive(true),
                LocalDate.of(1992, 10, 24),
                "Address");
    }
}
