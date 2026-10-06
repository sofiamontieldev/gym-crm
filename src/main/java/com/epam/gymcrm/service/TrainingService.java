package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDAO trainingDAO;
    private final AtomicLong idSequence = new AtomicLong(1);

    public TrainingService(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    public Training createTraining(
            Long traineeId,
            Long trainerId,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            int trainingDuration) {

        Training training = new Training(
                traineeId,
                trainerId,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration);

        Long trainingId = idSequence.getAndIncrement();
        trainingDAO.save(trainingId, training);
        log.info("Training created with id {}", trainingId);
        return training;
    }

    public Optional<Training> selectTraining(Long id) {
        return trainingDAO.findById(id);
    }

    public List<Training> selectAllTrainings() {
        return trainingDAO.findAll();
    }
}
