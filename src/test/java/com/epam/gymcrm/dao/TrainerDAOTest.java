package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerDAOTest {

    private Map<Long, Trainer> storage;
    private TrainerDAO trainerDAO;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainerDAO = new TrainerDAO(storage);
    }

    @Test
    void shouldSaveAndFindTrainerById() {
        Trainer trainer = createTrainer(10L);

        trainerDAO.save(trainer);

        assertEquals(java.util.Optional.of(trainer), trainerDAO.findById(10L));
    }

    @Test
    void shouldGenerateNextIdAfterExistingData() {
        storage.put(200L, createTrainer(200L));

        assertEquals(201L, trainerDAO.nextId());
    }

    private static Trainer createTrainer(Long id) {
        return new Trainer(
                new User.Builder()
                        .setId(id)
                        .setFirstName("Sara")
                        .setLastName("Martinez")
                        .setUsername("Sara.Martinez" + id)
                        .setPassword("Abc1234567")
                        .setActive(true),
                TrainingType.FITNESS);
    }
}
