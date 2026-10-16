package com.epam.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private int trainingDuration;

    protected Training() {
        // Required by Hibernate.
    }

    public Training(
            Trainee trainee,
            Trainer trainer,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            int trainingDuration) {

        if (trainee == null) {
            throw new IllegalArgumentException("trainee must not be null");
        }
        if (trainer == null) {
            throw new IllegalArgumentException("trainer must not be null");
        }
        if (trainingName == null || trainingName.isBlank()) {
            throw new IllegalArgumentException("trainingName must not be blank");
        }
        if (trainingType == null) {
            throw new IllegalArgumentException("trainingType must not be null");
        }
        if (trainingDate == null) {
            throw new IllegalArgumentException("trainingDate must not be null");
        }
        if (trainingDuration <= 0) {
            throw new IllegalArgumentException("trainingDuration must be greater than zero");
        }

        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        trainee.addTrainingInternal(this);
        trainer.addTrainingInternal(this);
    }

    public Long getId() {
        return id;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public Long getTraineeId() {
        return trainee.getId();
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public Long getTrainerId() {
        return trainer.getId();
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
