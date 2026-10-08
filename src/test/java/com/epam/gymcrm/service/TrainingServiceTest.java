package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.TrainingDAO;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingServiceTest {

    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingDAO = mock(TrainingDAO.class);
        traineeDAO = mock(TraineeDAO.class);
        trainerDAO = mock(TrainerDAO.class);
        trainingService = new TrainingService(trainingDAO, traineeDAO, trainerDAO);
    }

    @Test
    void shouldCreateTrainingWithPersistentRelations() {
        Trainee trainee = mock(Trainee.class);
        Trainer trainer = mock(Trainer.class);
        TrainingType fitness = new TrainingType("FITNESS");
        when(traineeDAO.findById(100L)).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(200L)).thenReturn(Optional.of(trainer));

        Training training = trainingService.createTraining(
                100L,
                200L,
                "Morning Fitness",
                fitness,
                LocalDate.of(2026, 10, 5),
                60);

        assertSame(trainee, training.getTrainee());
        assertSame(trainer, training.getTrainer());
        assertSame(fitness, training.getTrainingType());
        assertEquals("Morning Fitness", training.getTrainingName());
        assertEquals(60, training.getTrainingDuration());
        verify(trainingDAO).save(training);
    }

    @Test
    void shouldRejectTrainingWhenTraineeDoesNotExist() {
        when(traineeDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.createTraining(
                        999L,
                        200L,
                        "Morning Fitness",
                        new TrainingType("FITNESS"),
                        LocalDate.of(2026, 10, 5),
                        60));
    }

    @Test
    void shouldSelectTrainingAndAllTrainings() {
        Training training = mock(Training.class);
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(training));
        when(trainingDAO.findAll()).thenReturn(List.of(training));

        assertEquals(Optional.of(training), trainingService.selectTraining(1L));
        assertEquals(List.of(training), trainingService.selectAllTrainings());
    }
}
