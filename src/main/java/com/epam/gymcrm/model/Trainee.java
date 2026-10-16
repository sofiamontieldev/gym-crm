package com.epam.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String address;

    @ManyToMany
    @JoinTable(
            name = "Trainee2Trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_trainee_trainer",
                    columnNames = {"trainee_id", "trainer_id"}))
    private Set<Trainer> trainers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Training> trainings = new LinkedHashSet<>();

    protected Trainee() {
        // Required by Hibernate.
    }

    public Trainee(User user, LocalDate dateOfBirth, String address) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Trainer> getTrainers() {
        return Collections.unmodifiableSet(trainers);
    }

    public Set<Training> getTrainings() {
        return Collections.unmodifiableSet(trainings);
    }

    public void updatePersonalData(String firstName, String lastName) {
        user.updatePersonalData(firstName, lastName);
    }

    public void assignTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("trainer must not be null");
        }
        if (trainers.add(trainer)) {
            trainer.addTraineeInternal(this);
        }
    }

    public void replaceTrainers(Collection<Trainer> newTrainers) {
        Objects.requireNonNull(newTrainers, "trainers must not be null");
        Set<Trainer> replacements = new LinkedHashSet<>(newTrainers);
        if (replacements.contains(null)) {
            throw new IllegalArgumentException("trainers must not contain null");
        }

        Set<Trainer> removedTrainers = new LinkedHashSet<>(trainers);
        removedTrainers.removeAll(replacements);

        for (Trainer trainer : removedTrainers) {
            trainers.remove(trainer);
            trainer.removeTraineeInternal(this);
        }

        for (Trainer trainer : replacements) {
            assignTrainer(trainer);
        }
    }

    void addTrainingInternal(Training training) {
        trainings.add(training);
    }
}
