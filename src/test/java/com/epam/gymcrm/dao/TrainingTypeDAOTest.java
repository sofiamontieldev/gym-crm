package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TrainingTypeDAOTest {

    private final TrainingTypeDAO trainingTypeDAO;

    @Autowired
    TrainingTypeDAOTest(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

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
