package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDAO;
import org.springframework.stereotype.Service;

@Service
public class TrainingService {

    private final TrainingDAO trainingDAO;

    public TrainingService(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }
}