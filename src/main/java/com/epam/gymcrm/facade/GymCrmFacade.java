package com.epam.gymcrm.facade;

import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymCrmFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymCrmFacade(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService) {

        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTrainee(
            Long id,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        return traineeService.updateTrainee(id, firstName, lastName, dateOfBirth, address);
    }

    public void deleteTrainee(Long id) {
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> selectTrainee(Long id) {
        return traineeService.selectTrainee(id);
    }

    public List<Trainee> selectAllTrainees() {
        return traineeService.selectAllTrainees();
    }

    public Trainer createTrainer(
            String firstName,
            String lastName,
            TrainingType specialization) {

        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(
            Long id,
            String firstName,
            String lastName,
            TrainingType specialization) {

        return trainerService.updateTrainer(id, firstName, lastName, specialization);
    }

    public Optional<Trainer> selectTrainer(Long id) {
        return trainerService.selectTrainer(id);
    }

    public List<Trainer> selectAllTrainers() {
        return trainerService.selectAllTrainers();
    }

    public Training createTraining(
            Long traineeId,
            Long trainerId,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            int trainingDuration) {

        return trainingService.createTraining(
                traineeId,
                trainerId,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration);
    }

    public Optional<Training> selectTraining(Long id) {
        return trainingService.selectTraining(id);
    }

    public List<Training> selectAllTrainings() {
        return trainingService.selectAllTrainings();
    }
}
