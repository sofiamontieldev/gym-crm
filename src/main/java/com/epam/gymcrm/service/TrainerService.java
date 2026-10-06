package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDAO trainerDAO;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final AtomicLong idSequence = new AtomicLong(1);

    public TrainerService(
            TrainerDAO trainerDAO,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator) {

        this.trainerDAO = trainerDAO;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
    }

    public Trainer createTrainer(
            String firstName,
            String lastName,
            TrainingType specialization) {

        String username = usernameGenerator.generate(
                firstName,
                lastName,
                trainerDAO.findAll());

        Trainer trainer = new Trainer(
                new User.Builder()
                        .setId(idSequence.getAndIncrement())
                        .setFirstName(firstName)
                        .setLastName(lastName)
                        .setUsername(username)
                        .setPassword(passwordGenerator.generate())
                        .setActive(true),
                specialization);

        trainerDAO.save(trainer);
        log.info("Trainer created with id {} and username {}", trainer.getId(), trainer.getUsername());
        return trainer;
    }

    public Trainer updateTrainer(
            Long id,
            String firstName,
            String lastName,
            TrainingType specialization) {

        Trainer trainer = findRequired(id);
        trainer.updatePersonalData(firstName, lastName);
        trainer.setSpecialization(specialization);
        trainerDAO.save(trainer);

        log.info("Trainer updated with id {}", id);
        return trainer;
    }

    public Optional<Trainer> selectTrainer(Long id) {
        return trainerDAO.findById(id);
    }

    public List<Trainer> selectAllTrainers() {
        return trainerDAO.findAll();
    }

    private Trainer findRequired(Long id) {
        return trainerDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found with id: " + id));
    }
}
