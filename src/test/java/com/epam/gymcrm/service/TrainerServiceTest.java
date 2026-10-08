package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainerServiceTest {

    private TrainerDAO trainerDAO;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerDAO = mock(TrainerDAO.class);
        trainerService = new TrainerService(
                trainerDAO,
                new PasswordGenerator(),
                new UsernameGenerator());
    }

    @Test
    void shouldCreateTrainerWithSpecialization() {
        TrainingType fitness = new TrainingType("FITNESS");

        Trainer trainer = trainerService.createTrainer("Sara", "Martinez", fitness);

        assertEquals("Sara.Martinez", trainer.getUsername());
        assertEquals(10, trainer.getPassword().length());
        assertSame(fitness, trainer.getSpecialization());
        verify(trainerDAO).save(trainer);
    }

    @Test
    void shouldUpdateTrainerWithoutChangingCredentials() {
        TrainingType fitness = new TrainingType("FITNESS");
        TrainingType yoga = new TrainingType("YOGA");
        Trainer trainer = trainer(fitness);
        when(trainerDAO.findById(20L)).thenReturn(Optional.of(trainer));
        String username = trainer.getUsername();
        String password = trainer.getPassword();

        Trainer updated = trainerService.updateTrainer(20L, "Sara", "Gomez", yoga);

        assertSame(trainer, updated);
        assertEquals("Gomez", updated.getLastName());
        assertSame(yoga, updated.getSpecialization());
        assertEquals(username, updated.getUsername());
        assertEquals(password, updated.getPassword());
        verify(trainerDAO).save(trainer);
    }

    @Test
    void shouldFailWhenUpdatingUnknownTrainer() {
        when(trainerDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.updateTrainer(
                        999L, "Sara", "Martinez", new TrainingType("FITNESS")));
    }

    private static Trainer trainer(TrainingType specialization) {
        User user = new User.Builder()
                .setFirstName("Sara")
                .setLastName("Martinez")
                .setUsername("Sara.Martinez")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
        return new Trainer(user, specialization);
    }
}
