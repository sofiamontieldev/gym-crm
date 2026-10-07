package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingServiceTest {

    private Map<Long, Training> trainingStorage;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingStorage = new HashMap<>();
        trainingService = new TrainingService(new TrainingDAO(trainingStorage));
    }

    @Test
    void shouldCreateTraining() {
        Training training = trainingService.createTraining(
                100L,
                200L,
                "Morning Fitness",
                TrainingType.FITNESS,
                LocalDate.of(2026, 10, 5),
                60);

        assertSame(training, trainingStorage.get(1L));
        assertEquals(100L, training.getTraineeId());
        assertEquals(200L, training.getTrainerId());
        assertEquals("Morning Fitness", training.getTrainingName());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void shouldSelectTrainingAndAllTrainings() {
        Training first = trainingService.createTraining(
                100L, 200L, "Morning Fitness", TrainingType.FITNESS,
                LocalDate.of(2026, 10, 5), 60);
        trainingService.createTraining(
                100L, 200L, "Evening Yoga", TrainingType.YOGA,
                LocalDate.of(2026, 10, 6), 45);

        assertEquals(java.util.Optional.of(first), trainingService.selectTraining(1L));
        assertTrue(trainingService.selectTraining(999L).isEmpty());
        assertEquals(2, trainingService.selectAllTrainings().size());
    }

    private static Training createTraining() {
        return new Training(
                100L,
                200L,
                "Morning Fitness",
                TrainingType.FITNESS,
                LocalDate.of(2026, 10, 5),
                60);
    }
}
