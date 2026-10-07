package com.epam.gymcrm.loader;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class InitialDataLoader implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;
    private final Map<Long, Training> trainingStorage;
    private final UsernameGenerator usernameGenerator;
    private final String dataPath;

    private boolean loaded;

    public InitialDataLoader(
            @Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
            @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage,
            @Qualifier("trainingStorage") Map<Long, Training> trainingStorage,
            UsernameGenerator usernameGenerator,
            @Value("${gym.crm.initial-data.path}") String dataPath) {

        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        this.usernameGenerator = usernameGenerator;
        this.dataPath = dataPath;
    }

    @Override
    public void afterSingletonsInstantiated() {
        loadOnce();
    }

    private synchronized void loadOnce() {
        if (loaded) {
            return;
        }

        Resource resource = new DefaultResourceLoader().getResource(dataPath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                loadLine(line, lineNumber);
            }

            loaded = true;
            log.info(
                    "Initial data loaded: {} trainees, {} trainers, {} trainings",
                    traineeStorage.size(),
                    trainerStorage.size(),
                    trainingStorage.size());
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load initial data from " + dataPath,
                    exception);
        }
    }

    private void loadLine(String line, int lineNumber) {
        if (line.isBlank() || line.trim().startsWith("#")) {
            return;
        }

        String[] tokens = line.split(";", -1);
        String recordType = tokens[0].trim().toUpperCase();
        Map<String, String> values = new HashMap<>();

        for (int index = 1; index < tokens.length; index++) {
            String[] field = tokens[index].split("=", 2);
            if (field.length != 2 || field[0].isBlank()) {
                throw invalidLine(lineNumber, "Expected field in name=value format");
            }
            values.put(field[0].trim(), field[1].trim());
        }

        switch (recordType) {
            case "TRAINEE" -> loadTrainee(values, lineNumber);
            case "TRAINER" -> loadTrainer(values, lineNumber);
            case "TRAINING" -> loadTraining(values, lineNumber);
            default -> throw invalidLine(lineNumber, "Unknown record type: " + recordType);
        }
    }

    private void loadTrainee(Map<String, String> values, int lineNumber) {
        Long id = parseLong(values, "id", lineNumber);
        String firstName = required(values, "firstName", lineNumber);
        String lastName = required(values, "lastName", lineNumber);

        Trainee trainee = new Trainee(
                new User.Builder()
                        .setId(id)
                        .setFirstName(firstName)
                        .setLastName(lastName)
                        .setUsername(usernameGenerator.generate(firstName, lastName))
                        .setPassword(required(values, "password", lineNumber))
                        .setActive(Boolean.parseBoolean(required(values, "active", lineNumber))),
                LocalDate.parse(required(values, "dateOfBirth", lineNumber)),
                required(values, "address", lineNumber));

        putIfAbsent(traineeStorage, id, trainee, lineNumber);
    }

    private void loadTrainer(Map<String, String> values, int lineNumber) {
        Long id = parseLong(values, "id", lineNumber);
        String firstName = required(values, "firstName", lineNumber);
        String lastName = required(values, "lastName", lineNumber);

        Trainer trainer = new Trainer(
                new User.Builder()
                        .setId(id)
                        .setFirstName(firstName)
                        .setLastName(lastName)
                        .setUsername(usernameGenerator.generate(firstName, lastName))
                        .setPassword(required(values, "password", lineNumber))
                        .setActive(Boolean.parseBoolean(required(values, "active", lineNumber))),
                TrainingType.valueOf(required(values, "specialization", lineNumber).toUpperCase()));

        putIfAbsent(trainerStorage, id, trainer, lineNumber);
    }

    private void loadTraining(Map<String, String> values, int lineNumber) {
        Long id = parseLong(values, "id", lineNumber);

        Training training = new Training(
                parseLong(values, "traineeId", lineNumber),
                parseLong(values, "trainerId", lineNumber),
                required(values, "trainingName", lineNumber),
                TrainingType.valueOf(required(values, "trainingType", lineNumber).toUpperCase()),
                LocalDate.parse(required(values, "trainingDate", lineNumber)),
                Integer.parseInt(required(values, "trainingDuration", lineNumber)));

        putIfAbsent(trainingStorage, id, training, lineNumber);
    }

    private static <T> void putIfAbsent(
            Map<Long, T> storage,
            Long id,
            T value,
            int lineNumber) {

        if (storage.putIfAbsent(id, value) != null) {
            throw invalidLine(lineNumber, "Duplicated id: " + id);
        }
    }

    private static String required(
            Map<String, String> values,
            String field,
            int lineNumber) {

        String value = values.get(field);
        if (value == null || value.isBlank()) {
            throw invalidLine(lineNumber, "Missing field: " + field);
        }
        return value;
    }

    private static Long parseLong(
            Map<String, String> values,
            String field,
            int lineNumber) {

        try {
            return Long.valueOf(required(values, field, lineNumber));
        } catch (NumberFormatException exception) {
            throw invalidLine(lineNumber, "Invalid number in field: " + field);
        }
    }

    private static IllegalStateException invalidLine(int lineNumber, String message) {
        return new IllegalStateException("Invalid initial-data line " + lineNumber + ": " + message);
    }
}
