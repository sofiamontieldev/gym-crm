package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingTypeDAO;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDAO trainerDAO;
    private final TrainingTypeDAO trainingTypeDAO;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;

    public TrainerService(
            TrainerDAO trainerDAO,
            TrainingTypeDAO trainingTypeDAO,
            AuthenticationService authenticationService,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator) {

        this.trainerDAO = trainerDAO;
        this.trainingTypeDAO = trainingTypeDAO;
        this.authenticationService = authenticationService;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
    }

    @Transactional
    public Trainer createTrainer(
            String firstName,
            String lastName,
            String specializationName) {

        TrainingType specialization = findTrainingType(specializationName);
        String username = usernameGenerator.generate(firstName, lastName);
        User user = new User.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(username)
                .setPassword(passwordGenerator.generate())
                .setActive(true)
                .build();

        Trainer trainer = new Trainer(user, specialization);
        trainerDAO.save(trainer);
        log.info("Trainer created with id {} and username {}", trainer.getId(), username);
        return trainer;
    }

    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(String username, String password) {
        return requireAuthenticatedTrainer(username, password);
    }

    @Transactional(readOnly = true)
    public Trainer selectTrainer(String username, String password) {
        return requireAuthenticatedTrainer(username, password);
    }

    @Transactional
    public void changeTrainerPassword(
            String username,
            String currentPassword,
            String newPassword) {

        Trainer trainer = requireAuthenticatedTrainer(username, currentPassword);
        trainer.getUser().changePassword(newPassword);
        log.info("Trainer password changed for username {}", username);
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

    private TrainingType findTrainingType(String name) {
        return trainingTypeDAO.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Training type not found: {}", name);
                    return new IllegalArgumentException("Training type not found: " + name);
                });
    }
}
