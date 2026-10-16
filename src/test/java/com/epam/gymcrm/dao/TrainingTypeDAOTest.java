package com.epam.gymcrm.dao;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainingTypeDAOTest {

    @Autowired
    private TrainingTypeDAO trainingTypeDAO;

    @Test
    void findsSeededTrainingTypeByName() {
        TrainingType yoga = trainingTypeDAO.findByName("YOGA").orElseThrow();

        assertEquals("YOGA", yoga.getName());
        assertTrue(trainingTypeDAO.findByName("DESCONOCIDO").isEmpty());
    }

    @Test
    void listsTheFiveFixedTypesInNameOrder() {
        List<String> names = trainingTypeDAO.findAll().stream()
                .map(TrainingType::getName)
                .toList();

        assertEquals(
                List.of("FITNESS", "RESISTANCE", "STRETCHING", "YOGA", "ZUMBA"),
                names);
    }
}
