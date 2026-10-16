package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GymCrmFacadeTest {

    private TraineeService traineeService;
    private TrainingService trainingService;
    private GymCrmFacade facade;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        TrainerService trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        facade = new GymCrmFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void delegatesTraineeRegistration() {
        Trainee expected = mock(Trainee.class);
        LocalDate dateOfBirth = LocalDate.of(1995, 4, 12);
        when(traineeService.createTrainee(
                "Valentina",
                "Rojas",
                dateOfBirth,
                "Cra. 43A # 10-20"))
                .thenReturn(expected);

        Trainee result = facade.createTrainee(
                "Valentina",
                "Rojas",
                dateOfBirth,
                "Cra. 43A # 10-20");

        assertSame(expected, result);
        verify(traineeService).createTrainee(
                "Valentina",
                "Rojas",
                dateOfBirth,
                "Cra. 43A # 10-20");
    }

    @Test
    void delegatesAuthenticatedTrainingCreation() {
        Training expected = mock(Training.class);
        LocalDate date = LocalDate.of(2026, 10, 5);
        when(trainingService.createTraining(
                "Andres.Gomez",
                "Abc1234567",
                "Valentina.Rojas",
                "Andres.Gomez",
                "Morning Fitness",
                "FITNESS",
                date,
                60))
                .thenReturn(expected);

        Training result = facade.createTraining(
                "Andres.Gomez",
                "Abc1234567",
                "Valentina.Rojas",
                "Andres.Gomez",
                "Morning Fitness",
                "FITNESS",
                date,
                60);

        assertSame(expected, result);
        verify(trainingService).createTraining(
                "Andres.Gomez",
                "Abc1234567",
                "Valentina.Rojas",
                "Andres.Gomez",
                "Morning Fitness",
                "FITNESS",
                date,
                60);
    }
}
