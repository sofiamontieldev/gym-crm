package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDAO traineeDAO;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final AtomicLong idSequence = new AtomicLong(1);

    public TraineeService(
            TraineeDAO traineeDAO,
            PasswordGenerator passwordGenerator,
            UsernameGenerator usernameGenerator) {

        this.traineeDAO = traineeDAO;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
    }

    public Trainee createTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        String username = usernameGenerator.generate(
                firstName,
                lastName,
                traineeDAO.findAll());

        Trainee trainee = new Trainee(
                new User.Builder()
                        .setId(idSequence.getAndIncrement())
                        .setFirstName(firstName)
                        .setLastName(lastName)
                        .setUsername(username)
                        .setPassword(passwordGenerator.generate())
                        .setActive(true),
                dateOfBirth,
                address);

        traineeDAO.save(trainee);
        log.info("Trainee created with id {} and username {}", trainee.getId(), trainee.getUsername());
        return trainee;
    }

    public Trainee updateTrainee(
            Long id,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address) {

        Trainee trainee = findRequired(id);
        trainee.updatePersonalData(firstName, lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        traineeDAO.save(trainee);

        log.info("Trainee updated with id {}", id);
        return trainee;
    }

    public void deleteTrainee(Long id) {
        findRequired(id);
        traineeDAO.deleteById(id);
        log.info("Trainee deleted with id {}", id);
    }

    public Optional<Trainee> selectTrainee(Long id) {
        return traineeDAO.findById(id);
    }

    public List<Trainee> selectAllTrainees() {
        return traineeDAO.findAll();
    }

    private Trainee findRequired(Long id) {
        return traineeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee not found with id: " + id));
    }
}
