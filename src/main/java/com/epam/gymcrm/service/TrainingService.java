package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDAO trainingDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;

    public TrainingService(
            TrainingDAO trainingDAO,
            TraineeDAO traineeDAO,
            TrainerDAO trainerDAO) {
        this.trainingDAO = trainingDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
    }

    @Transactional
    public Training createTraining(
            Long traineeId,
            Long trainerId,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            int trainingDuration) {

        Trainee trainee = traineeDAO.findById(traineeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee not found with id: " + traineeId));
        Trainer trainer = trainerDAO.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found with id: " + trainerId));

        Training training = new Training(
                trainee,
                trainer,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration);

        trainingDAO.save(training);
        log.info("Training created with id {}", training.getId());
        return training;
    }

    @Transactional(readOnly = true)
    public Optional<Training> selectTraining(Long id) {
        return trainingDAO.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Training> selectAllTrainings() {
        return trainingDAO.findAll();
    }
}
