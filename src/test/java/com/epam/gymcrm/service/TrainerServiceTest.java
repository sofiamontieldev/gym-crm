package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerServiceTest {

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(
                new TrainerDAO(new HashMap<>()),
                new PasswordGenerator(),
                new UsernameGenerator());
    }

    @Test
    void shouldCreateTrainerWithSpecialization() {
        Trainer trainer = trainerService.createTrainer(
                "Sara", "Martinez", TrainingType.FITNESS);

        assertEquals(1L, trainer.getId());
        assertEquals("Sara.Martinez", trainer.getUsername());
        assertEquals(10, trainer.getPassword().length());
        assertEquals(TrainingType.FITNESS, trainer.getSpecialization());
        assertTrue(trainer.isActive());
    }

    @Test
    void shouldUpdateTrainerWithoutChangingCredentials() {
        Trainer trainer = trainerService.createTrainer(
                "Sara", "Martinez", TrainingType.FITNESS);
        String username = trainer.getUsername();
        String password = trainer.getPassword();

        Trainer updated = trainerService.updateTrainer(
                trainer.getId(),
                "Sara",
                "Gomez",
                TrainingType.YOGA);

        assertSame(trainer, updated);
        assertEquals("Gomez", updated.getLastName());
        assertEquals(TrainingType.YOGA, updated.getSpecialization());
        assertEquals(username, updated.getUsername());
        assertEquals(password, updated.getPassword());
    }

    @Test
    void shouldFailWhenUpdatingUnknownTrainer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.updateTrainer(
                        999L, "Sara", "Martinez", TrainingType.FITNESS));
    }

}
