package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDAOTest {

    private Map<Long, Trainee> storage;
    private TraineeDAO traineeDAO;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        traineeDAO = new TraineeDAO(storage);
    }

    @Test
    void shouldSaveAndFindTraineeById() {
        Trainee trainee = createTrainee(10L);

        traineeDAO.save(trainee);

        assertEquals(java.util.Optional.of(trainee), traineeDAO.findById(10L));
    }

    @Test
    void shouldDeleteTraineeById() {
        traineeDAO.save(createTrainee(10L));

        traineeDAO.deleteById(10L);

        assertTrue(traineeDAO.findById(10L).isEmpty());
        assertFalse(storage.containsKey(10L));
    }

    @Test
    void shouldGenerateNextIdAfterExistingData() {
        storage.put(100L, createTrainee(100L));
        storage.put(25L, createTrainee(25L));

        assertEquals(101L, traineeDAO.nextId());
    }

    private static Trainee createTrainee(Long id) {
        return new Trainee(
                new User.Builder()
                        .setId(id)
                        .setFirstName("John")
                        .setLastName("Smith")
                        .setUsername("John.Smith" + id)
                        .setPassword("Abc1234567")
                        .setActive(true),
                LocalDate.of(1995, 4, 12),
                "Main Street 123");
    }
}
