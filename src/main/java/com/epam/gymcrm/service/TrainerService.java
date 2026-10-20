package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingTypeDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.model.UserRole;
import com.epam.gymcrm.monitoring.GymMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDAO trainerDAO;
    private final TrainingTypeDAO trainingTypeDAO;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics gymMetrics;

    public TrainerService(
            TrainerDAO trainerDAO,
            TrainingTypeDAO trainingTypeDAO,
            AuthenticationService authenticationService,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator,
            PasswordEncoder passwordEncoder,
            GymMetrics gymMetrics) {

        this.trainerDAO = trainerDAO;
        this.trainingTypeDAO = trainingTypeDAO;
        this.authenticationService = authenticationService;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordEncoder = passwordEncoder;
        this.gymMetrics = gymMetrics;
    }

    @Transactional
    public Trainer createTrainer(
            String firstName,
            String lastName,
            String specializationName) {

        return registerTrainer(firstName, lastName, specializationName).profile();
    }

    @Transactional
    public RegistrationResult<Trainer> registerTrainer(
            String firstName,
            String lastName,
            String specializationName) {

        TrainingType specialization = findTrainingType(specializationName);
        String username = usernameGenerator.generate(firstName, lastName);
        String password = passwordGenerator.generate();
        User user = new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword(passwordEncoder.encode(password))
                .setRole(UserRole.TRAINER)
                .setActive(true)
                .build();

        Trainer trainer = new Trainer(user, specialization);
        trainerDAO.save(trainer);
        gymMetrics.profileRegistered(UserRole.TRAINER);
        log.info("Trainer created with id {} and username {}", trainer.getId(), username);
        return new RegistrationResult<>(trainer, username, password);
    }

    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(String username, String password) {
        return requireAuthenticatedTrainer(username, password);
    }

    @Transactional(readOnly = true)
    public Trainer selectTrainer(String username, String password) {
        return requireAuthenticatedTrainer(username, password);
    }

    @Transactional(readOnly = true)
    public Trainer selectTrainer(String username) {
        return trainerDAO.findProfileByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));
    }

    @Transactional
    public void changeTrainerPassword(
            String username,
            String currentPassword,
            String newPassword) {

        authenticationService.changePassword(username, currentPassword, newPassword);
    }

    @Transactional
    public Trainer updateTrainer(
            String username,
            String password,
            String firstName,
            String lastName,
            String specializationName) {

        Trainer trainer = requireAuthenticatedTrainer(username, password);
        trainer.updatePersonalData(firstName, lastName);
        trainer.setSpecialization(findTrainingType(specializationName));
        log.info("Trainer updated for username {}", username);
        return trainer;
    }

    @Transactional
    public Trainer updateTrainer(
            String username,
            String firstName,
            String lastName,
            boolean active) {

        Trainer trainer = selectTrainer(username);
        trainer.updatePersonalData(firstName, lastName);
        trainer.getUser().setActive(active);
        log.info("Trainer updated for username {}", username);
        return trainer;
    }

    @Transactional
    public void activateTrainer(String username, String password) {
        authenticationService.verifyCredentials(username, password);
        Trainer trainer = findTrainerForCredentials(username);
        trainer.getUser().activate();
        log.info("Trainer activated for username {}", username);
    }

    @Transactional
    public void deactivateTrainer(String username, String password) {
        authenticationService.verifyCredentials(username, password);
        Trainer trainer = findTrainerForCredentials(username);
        trainer.getUser().deactivate();
        log.info("Trainer deactivated for username {}", username);
    }

    private Trainer requireAuthenticatedTrainer(String username, String password) {
        authenticationService.authenticate(username, password);
        return findTrainerForCredentials(username);
    }

    private Trainer findTrainerForCredentials(String username) {
        return trainerDAO.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException(
                        "Authenticated user is not a trainer"));
    }

    private Trainer findTrainer(String username) {
        return trainerDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));
    }

    private TrainingType findTrainingType(String name) {
        return trainingTypeDAO.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Training type not found: {}", name);
                    return new IllegalArgumentException("Training type not found: " + name);
                });
    }
}
