package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDAOTest {

    private Map<Long, Training> storage;
    private TrainingDAO trainingDAO;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDAO = new TrainingDAO(storage);
    }

    @Test
    void shouldSaveAndFindTrainingById() {
        Training training = createTraining();

        trainingDAO.save(10L, training);

        assertEquals(java.util.Optional.of(training), trainingDAO.findById(10L));
    }

    @Test
    void shouldGenerateNextIdAfterExistingData() {
        storage.put(300L, createTraining());

        assertEquals(301L, trainingDAO.nextId());
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
