package com.epam.gymcrm.api;

import com.epam.gymcrm.api.dto.ApiDtos;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public ApiDtos.TraineeProfileResponse toTraineeProfile(Trainee trainee) {
        return new ApiDtos.TraineeProfileResponse(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainee.getTrainers().stream().map(this::toTrainerSummary).toList());
    }

    public ApiDtos.TraineeUpdateResponse toTraineeUpdate(Trainee trainee) {
        return new ApiDtos.TraineeUpdateResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainee.getTrainers().stream().map(this::toTrainerSummary).toList());
    }

    public ApiDtos.TrainerProfileResponse toTrainerProfile(Trainer trainer) {
        return new ApiDtos.TrainerProfileResponse(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getName(),
                trainer.isActive(),
                trainer.getTrainees().stream().map(this::toTraineeSummary).toList());
    }

    public ApiDtos.TrainerUpdateResponse toTrainerUpdate(Trainer trainer) {
        return new ApiDtos.TrainerUpdateResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getName(),
                trainer.isActive(),
                trainer.getTrainees().stream().map(this::toTraineeSummary).toList());
    }

    public ApiDtos.TrainerSummary toTrainerSummary(Trainer trainer) {
        return new ApiDtos.TrainerSummary(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getName());
    }

    public ApiDtos.TraineeSummary toTraineeSummary(Trainee trainee) {
        return new ApiDtos.TraineeSummary(
                trainee.getUsername(), trainee.getFirstName(), trainee.getLastName());
    }

    public ApiDtos.TrainingResponse toTraining(Training training) {
        return new ApiDtos.TrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getName(),
                training.getTrainingDuration(),
                training.getTrainee().getUsername(),
                training.getTrainer().getUsername());
    }

    public ApiDtos.TrainingTypeResponse toTrainingType(TrainingType trainingType) {
        return new ApiDtos.TrainingTypeResponse(trainingType.getId(), trainingType.getName());
    }
}
