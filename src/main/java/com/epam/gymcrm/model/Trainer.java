package com.epam.gymcrm.model;

public class Trainer extends User {

    private TrainingType specialization;

    public Trainer(
            User.Builder userBuilder,
            TrainingType specialization) {

        super(userBuilder);

        this.specialization = specialization;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }
}
