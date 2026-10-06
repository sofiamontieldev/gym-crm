package com.epam.gymcrm.config;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
class StorageConfigTest {

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Integer, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Integer, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Integer, Training> trainingStorage;

    @Test
    void createsThreeIndependentStorages() {
        assertTrue(traineeStorage.isEmpty());
        assertTrue(trainerStorage.isEmpty());
        assertTrue(trainingStorage.isEmpty());

        assertNotSame(traineeStorage, trainerStorage);
        assertNotSame(traineeStorage, trainingStorage);
        assertNotSame(trainerStorage, trainingStorage);
    }
}
