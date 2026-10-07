package com.epam.gymcrm.config;

import com.epam.gymcrm.facade.GymCrmFacade;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(AppConfig.class)
public class SpringContextTest {

    @Autowired
    private GymCrmFacade facade;

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Long, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Long, Training> trainingStorage;

    @Test
    void springContextStartsAndLoadsInitialData() {
        assertNotNull(facade);
        assertEquals(1, traineeStorage.size());
        assertEquals(1, trainerStorage.size());
        assertEquals(1, trainingStorage.size());
        assertEquals(100L, trainingStorage.get(300L).getTraineeId());
        assertEquals(200L, trainingStorage.get(300L).getTrainerId());

    }

}
