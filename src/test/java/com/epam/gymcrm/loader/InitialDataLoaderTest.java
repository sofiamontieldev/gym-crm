package com.epam.gymcrm.loader;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InitialDataLoaderTest {

    @TempDir
    Path temporaryDirectory;

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;

    @Test
    void shouldLoadSemicolonSeparatedInitialData() throws IOException {
        InitialDataLoader loader = createLoader(
                "TRAINEE;id=100;firstName=Alicia;lastName=Montoya;password=FirstPass1;active=true;dateOfBirth=1992-10-24;address=Calle 63 # 42-80\n" +
                        "TRAINER;id=200;firstName=Sara;lastName=Martinez;password=SecondPass2;active=true;specialization=FITNESS\n" +
                        "TRAINING;id=300;traineeId=100;trainerId=200;trainingName=Morning Fitness;trainingType=FITNESS;trainingDate=2026-10-05;trainingDuration=60\n");

        loader.afterSingletonsInstantiated();

        assertEquals(1, traineeStorage.size());
        assertEquals(1, trainerStorage.size());
        assertEquals(1, trainingStorage.size());
        assertEquals("Alicia.Montoya", traineeStorage.get(100L).getUsername());
        assertEquals(100L, trainingStorage.get(300L).getTraineeId());
        assertEquals(200L, trainingStorage.get(300L).getTrainerId());
    }

    @Test
    void shouldRejectDuplicatedIds() throws IOException {
        InitialDataLoader loader = createLoader(
                "TRAINEE;id=100;firstName=Alicia;lastName=Montoya;password=FirstPass1;active=true;dateOfBirth=1992-10-24;address=Address\n" +
                        "TRAINEE;id=100;firstName=Another;lastName=Person;password=SecondPass2;active=true;dateOfBirth=1990-01-01;address=Address\n");

        assertThrows(IllegalStateException.class, loader::afterSingletonsInstantiated);
    }

    private InitialDataLoader createLoader(String content) throws IOException {
        Path dataFile = temporaryDirectory.resolve("initial-data.csv");
        Files.writeString(dataFile, content);

        traineeStorage = new HashMap<>();
        trainerStorage = new HashMap<>();
        trainingStorage = new HashMap<>();

        return new InitialDataLoader(
                traineeStorage,
                trainerStorage,
                trainingStorage,
                new UsernameGenerator(),
                dataFile.toUri().toString());
    }
}
