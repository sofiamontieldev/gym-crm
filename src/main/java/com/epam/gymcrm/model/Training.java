package com.epam.gymcrm.model;

import java.time.LocalDate;

public class Training {

    private final Long traineeId;
    private final Long trainerId;
    private final String trainingName;
    private final TrainingType trainingType;
    private final LocalDate trainingDate;
    private final int trainingDuration;

    public Training(
            Long traineeId,
            Long trainerId,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            int trainingDuration) {

        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Long getTraineeId() {
        return traineeId;
    }
    public Long getTrainerId() {
        return trainerId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }
}
