package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingTypeDAO;
import com.epam.gymcrm.model.TrainingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {

    private final TrainingTypeDAO trainingTypeDAO;

    public TrainingTypeService(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        return trainingTypeDAO.findAll();
    }
}
