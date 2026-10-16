package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;

    public TraineeService(
            TraineeDAO traineeDAO,
            TrainerDAO trainerDAO,
            AuthenticationService authenticationService,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator) {

        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.authenticationService = authenticationService;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
    }

    @Transactional
    public Trainee createTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        String username = usernameGenerator.generate(firstName, lastName);
        User user = new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword(passwordGenerator.generate())
                .setActive(true)
                .build();

        Trainee trainee = new Trainee(user, dateOfBirth, address);
        traineeDAO.save(trainee);
        log.info("Trainee created with id {} and username {}", trainee.getId(), username);
        return trainee;
    }

    @Transactional(readOnly = true)
    public Trainee authenticateTrainee(String username, String password) {
        return requireAuthenticatedTrainee(username, password);
    }

    @Transactional(readOnly = true)
    public Trainee selectTrainee(String username, String password) {
        authenticationService.authenticate(username, password);
        return traineeDAO.findProfileByUsername(username)
                .orElseThrow(TraineeService::invalidTraineeCredentials);
    }

    @Transactional
    public void changeTraineePassword(
            String username,
            String currentPassword,
            String newPassword) {

        Trainee trainee = requireAuthenticatedTrainee(username, currentPassword);
        trainee.getUser().changePassword(newPassword);
        log.info("Trainee password changed for username {}", username);
    }

    @Transactional
    public Trainee updateTrainee(
            String username,
            String password,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        Trainee trainee = requireAuthenticatedTrainee(username, password);
        trainee.updatePersonalData(firstName, lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        log.info("Trainee updated for username {}", username);
        return trainee;
    }

    @Transactional
    public void activateTrainee(String username, String password) {
        authenticationService.verifyCredentials(username, password);
        Trainee trainee = findTraineeForCredentials(username);
        trainee.getUser().activate();
        log.info("Trainee activated for username {}", username);
    }

    @Transactional
    public void deactivateTrainee(String username, String password) {
        authenticationService.verifyCredentials(username, password);
        Trainee trainee = findTraineeForCredentials(username);
        trainee.getUser().deactivate();
        log.info("Trainee deactivated for username {}", username);
    }

    @Transactional
    public void deleteTrainee(String username, String password) {
        Trainee trainee = requireAuthenticatedTrainee(username, password);
        traineeDAO.delete(trainee);
        log.info("Trainee deleted for username {}", username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> selectActiveUnassignedTrainers(String username, String password) {
        Trainee trainee = requireAuthenticatedTrainee(username, password);
        return trainerDAO.findActiveNotAssignedTo(trainee);
    }

    @Transactional
    public void replaceTrainers(
            String username,
            String password,
            Collection<String> trainerUsernames) {

        Trainee trainee = requireAuthenticatedTrainee(username, password);
        Set<String> requestedUsernames = validateTrainerUsernames(trainerUsernames);
        List<Trainer> trainers = trainerDAO.findAllByUsernames(requestedUsernames);

        if (trainers.size() != requestedUsernames.size()) {
            throw new IllegalArgumentException("One or more trainers do not exist");
        }
        if (trainers.stream().anyMatch(trainer -> !trainer.isActive())) {
            throw new IllegalArgumentException("All assigned trainers must be active");
        }

        traineeDAO.replaceTrainers(trainee, trainers);
        log.info("Trainer assignments updated for trainee {}: {} assigned", username, trainers.size());
    }

    private Trainee requireAuthenticatedTrainee(String username, String password) {
        authenticationService.authenticate(username, password);
        return findTraineeForCredentials(username);
    }

    private Trainee findTraineeForCredentials(String username) {
        return traineeDAO.findByUsername(username)
                .orElseThrow(TraineeService::invalidTraineeCredentials);
    }

    private static AuthenticationException invalidTraineeCredentials() {
        return new AuthenticationException("Authenticated user is not a trainee");
    }

    private static Set<String> validateTrainerUsernames(Collection<String> trainerUsernames) {
        Objects.requireNonNull(trainerUsernames, "trainerUsernames must not be null");
        Set<String> uniqueUsernames = new LinkedHashSet<>();
        for (String trainerUsername : trainerUsernames) {
            if (trainerUsername == null || trainerUsername.isBlank()) {
                throw new IllegalArgumentException("trainerUsername must not be blank");
            }
            uniqueUsernames.add(trainerUsername);
        }
        return uniqueUsernames;
    }
}
