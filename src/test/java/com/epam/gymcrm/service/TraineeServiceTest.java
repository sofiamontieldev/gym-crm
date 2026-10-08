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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraineeServiceTest {

    private TraineeDAO traineeDAO;
    private PasswordGenerator passwordGenerator;
    private UsernameGenerator usernameGenerator;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(TraineeDAO.class);
        passwordGenerator = mock(PasswordGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        traineeService = new TraineeService(traineeDAO, passwordGenerator, usernameGenerator);
    }

    @Test
    void shouldCreateTraineeWithGeneratedCredentials() {
        when(usernameGenerator.generate("Pablo", "Ordoñez")).thenReturn("Pablo.Ordoñez");
        when(passwordGenerator.generate()).thenReturn("Abc1234567");

        Trainee trainee = traineeService.createTrainee(
                "Pablo",
                "Ordoñez",
                LocalDate.of(1995, 4, 12),
                "Main Street 123");

        assertEquals("Pablo.Ordoñez", trainee.getUsername());
        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());
        assertTrue(trainee.isActive());
        verify(traineeDAO).save(trainee);
    }

    @Test
    void shouldGenerateUniqueUsernameAcrossTraineesAndTrainers() {
        UsernameGenerator sharedGenerator = new UsernameGenerator();
        TraineeService localTraineeService = new TraineeService(
                mock(TraineeDAO.class), new PasswordGenerator(), sharedGenerator);
        TrainerService trainerService = new TrainerService(
                mock(TrainerDAO.class), new PasswordGenerator(), sharedGenerator);

        Trainee trainee = localTraineeService.createTrainee(
                "Pablo", "Ordoñez", LocalDate.of(1995, 4, 12), "Address");
        Trainer trainer = trainerService.createTrainer(
                "Pablo", "Ordoñez", new TrainingType("FITNESS"));

        assertEquals("Pablo.Ordoñez", trainee.getUsername());
        assertEquals("Pablo.Ordoñez1", trainer.getUsername());
    }

    @Test
    void shouldUpdateTraineeWithoutChangingCredentials() {
        Trainee trainee = trainee();
        when(traineeDAO.findById(10L)).thenReturn(Optional.of(trainee));
        String username = trainee.getUsername();
        String password = trainee.getPassword();

        Trainee updated = traineeService.updateTrainee(
                10L,
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
        verify(traineeDAO).save(trainee);
    }

    @Test
    void shouldFailWhenUpdatingUnknownTrainee() {
        when(traineeDAO.findById(999L)).thenReturn(Optional.empty());

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
    void shouldDeleteExistingTrainee() {
        when(traineeDAO.findById(10L)).thenReturn(Optional.of(trainee()));

        traineeService.deleteTrainee(10L);

        verify(traineeDAO).deleteById(10L);
    }

    private static Trainee trainee() {
        User user = new User.Builder()
                .setFirstName("Alicia")
                .setLastName("Montoya")
                .setUsername("Alicia.Montoya")
                .setPassword("FirstPass1")
                .setActive(true)
                .build();
        return new Trainee(user, LocalDate.of(1992, 10, 24), "Address");
    }
}
