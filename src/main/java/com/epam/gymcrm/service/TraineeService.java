package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.model.UserRole;
import com.epam.gymcrm.monitoring.GymMetrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics gymMetrics;

    public TraineeService(
            TraineeDAO traineeDAO,
            TrainerDAO trainerDAO,
            AuthenticationService authenticationService,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator,
            PasswordEncoder passwordEncoder,
            GymMetrics gymMetrics) {

        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.authenticationService = authenticationService;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordEncoder = passwordEncoder;
        this.gymMetrics = gymMetrics;
    }

    @Transactional
    public Trainee createTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        return registerTrainee(firstName, lastName, dateOfBirth, address).profile();
    }

    @Transactional
    public RegistrationResult<Trainee> registerTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        String username = usernameGenerator.generate(firstName, lastName);
        String password = passwordGenerator.generate();
        User user = new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword(passwordEncoder.encode(password))
                .setRole(UserRole.TRAINEE)
                .setActive(true)
                .build();

        Trainee trainee = new Trainee(user, dateOfBirth, address);
        traineeDAO.save(trainee);
        gymMetrics.profileRegistered(UserRole.TRAINEE);

        log.info(
                "Trainee created with id {} and username {}",
                trainee.getId(),
                username);
        return new RegistrationResult<>(trainee, username, password);
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

    @Transactional(readOnly = true)
    public Trainee selectTrainee(String username) {
        return traineeDAO.findProfileByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));
    }

    @Transactional
    public void changeTraineePassword(
            String username,
            String currentPassword,
            String newPassword) {

        authenticationService.changePassword(username, currentPassword, newPassword);
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
    public Trainee updateTrainee(
            String username,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            boolean active) {

        Trainee trainee = selectTrainee(username);
        trainee.updatePersonalData(firstName, lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.getUser().setActive(active);
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

    @Transactional
    public void deleteTrainee(String username) {
        traineeDAO.delete(findTrainee(username));
        log.info("Trainee deleted for username {}", username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> selectActiveUnassignedTrainers(String username, String password) {
        Trainee trainee = requireAuthenticatedTrainee(username, password);
        return trainerDAO.findActiveNotAssignedTo(trainee);
    }

    @Transactional(readOnly = true)
    public List<Trainer> selectActiveUnassignedTrainers(String username) {
        return trainerDAO.findActiveNotAssignedTo(findTrainee(username));
    }

    @Transactional
    public void replaceTrainers(
            String username,
            String password,
            Collection<String> trainerUsernames) {

        Trainee trainee = requireAuthenticatedTrainee(username, password);
        replaceTrainers(trainee, username, trainerUsernames);
    }

    @Transactional
    public void replaceTrainers(String username, Collection<String> trainerUsernames) {
        replaceTrainers(findTrainee(username), username, trainerUsernames);
    }

    private void replaceTrainers(
            Trainee trainee,
            String username,
            Collection<String> trainerUsernames) {
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

    private Trainee findTrainee(String username) {
        return traineeDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));
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
