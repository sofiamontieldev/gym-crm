package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

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

    public Trainer createTrainer(
            String firstName,
            String lastName,
            String specializationName) {

        return trainerService.createTrainer(firstName, lastName, specializationName);
    }

    public Trainee authenticateTrainee(String username, String password) {
        return traineeService.authenticateTrainee(username, password);
    }

    public Trainer authenticateTrainer(String username, String password) {
        return trainerService.authenticateTrainer(username, password);
    }

    public Trainee selectTrainee(String username, String password) {
        return traineeService.selectTrainee(username, password);
    }

    public Trainer selectTrainer(String username, String password) {
        return trainerService.selectTrainer(username, password);
    }

    public void changeTraineePassword(
            String username,
            String currentPassword,
            String newPassword) {

        traineeService.changeTraineePassword(username, currentPassword, newPassword);
    }

    public void changeTrainerPassword(
            String username,
            String currentPassword,
            String newPassword) {

        trainerService.changeTrainerPassword(username, currentPassword, newPassword);
    }

    public Trainee updateTrainee(
            String username,
            String password,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        return traineeService.updateTrainee(
                username,
                password,
                firstName,
                lastName,
                dateOfBirth,
                address);
    }

    public Trainer updateTrainer(
            String username,
            String password,
            String firstName,
            String lastName,
            String specializationName) {

        return trainerService.updateTrainer(
                username,
                password,
                firstName,
                lastName,
                specializationName);
    }

    public void activateTrainee(String username, String password) {
        traineeService.activateTrainee(username, password);
    }

    public void deactivateTrainee(String username, String password) {
        traineeService.deactivateTrainee(username, password);
    }

    public void activateTrainer(String username, String password) {
        trainerService.activateTrainer(username, password);
    }

    public void deactivateTrainer(String username, String password) {
        trainerService.deactivateTrainer(username, password);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteTrainee(username, password);
    }

    public Training createTraining(
            String authUsername,
            String authPassword,
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            String trainingTypeName,
            LocalDate trainingDate,
            int trainingDuration) {

        return trainingService.createTraining(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingTypeName,
                trainingDate,
                trainingDuration);
    }

    public List<Training> selectTraineeTrainings(
            String traineeUsername,
            String password,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName) {

        return trainingService.selectTraineeTrainings(
                traineeUsername,
                password,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName);
    }

    public List<Training> selectTrainerTrainings(
            String trainerUsername,
            String password,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        return trainingService.selectTrainerTrainings(
                trainerUsername,
                password,
                fromDate,
                toDate,
                traineeUsername);
    }

    public List<Trainer> selectActiveUnassignedTrainers(
            String traineeUsername,
            String password) {

        return traineeService.selectActiveUnassignedTrainers(traineeUsername, password);
    }

    public void replaceTrainers(
            String traineeUsername,
            String password,
            Collection<String> trainerUsernames) {

        traineeService.replaceTrainers(traineeUsername, password, trainerUsernames);
    }
}
