package com.epam.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new LinkedHashSet<>();

    @OneToMany(mappedBy = "trainer")
    private Set<Training> trainings = new LinkedHashSet<>();

    protected Trainer() {
        // Required by Hibernate.
    }

    public Trainer(User user, TrainingType specialization) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        if (specialization == null) {
            throw new IllegalArgumentException("specialization must not be null");
        }
        this.user = user;
        this.specialization = specialization;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getPassword() {
        return user.getPassword();
    }

    public boolean isActive() {
        return user.isActive();
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        if (specialization == null) {
            throw new IllegalArgumentException("specialization must not be null");
        }
        this.specialization = specialization;
    }

    public Set<Trainee> getTrainees() {
        return Collections.unmodifiableSet(trainees);
    }

    public Set<Training> getTrainings() {
        return Collections.unmodifiableSet(trainings);
    }

    public void updatePersonalData(String firstName, String lastName) {
        user.updatePersonalData(firstName, lastName);
    }

    void addTraineeInternal(Trainee trainee) {
        trainees.add(trainee);
    }

    void removeTraineeInternal(Trainee trainee) {
        trainees.remove(trainee);
    }

    void addTrainingInternal(Training training) {
        trainings.add(training);
    }
}
