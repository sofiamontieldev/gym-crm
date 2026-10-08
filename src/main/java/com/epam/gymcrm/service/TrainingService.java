package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.dao.TrainingTypeDAO;
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

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDAO trainingDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final TrainingTypeDAO trainingTypeDAO;
    private final AuthenticationService authenticationService;

    public TrainingService(
            TrainingDAO trainingDAO,
            TraineeDAO traineeDAO,
            TrainerDAO trainerDAO,
            TrainingTypeDAO trainingTypeDAO,
            AuthenticationService authenticationService) {

        this.trainingDAO = trainingDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.trainingTypeDAO = trainingTypeDAO;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public Training createTraining(
            String authUsername,
            String authPassword,
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            String trainingTypeName,
            LocalDate trainingDate,
            int trainingDuration) {

        authenticationService.authenticate(authUsername, authPassword);
        Trainee trainee = findTrainee(traineeUsername);
        Trainer trainer = findTrainer(trainerUsername);
        TrainingType trainingType = findTrainingType(trainingTypeName);

        Training training = new Training(
                trainee,
                trainer,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration);

        trainingDAO.save(training);
        log.info(
                "Training created with id {} for trainee {} and trainer {}",
                training.getId(),
                traineeUsername,
                trainerUsername);
        return training;
    }

    @Transactional(readOnly = true)
    public List<Training> selectTraineeTrainings(
            String traineeUsername,
            String password,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName) {

        authenticationService.authenticate(traineeUsername, password);
        findTrainee(traineeUsername);
        validateDateRange(fromDate, toDate);
        return trainingDAO.findByTraineeCriteria(
                traineeUsername,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Training> selectTrainerTrainings(
            String trainerUsername,
            String password,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        authenticationService.authenticate(trainerUsername, password);
        findTrainer(trainerUsername);
        validateDateRange(fromDate, toDate);
        return trainingDAO.findByTrainerCriteria(
                trainerUsername,
                fromDate,
                toDate,
                traineeUsername);
    }

    private Trainee findTrainee(String username) {
        return traineeDAO.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee not found: " + username));
    }

    private Trainer findTrainer(String username) {
        return trainerDAO.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found: " + username));
    }

    private TrainingType findTrainingType(String name) {
        return trainingTypeDAO.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Training type not found: " + name));
    }

    private static void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }
    }
}
