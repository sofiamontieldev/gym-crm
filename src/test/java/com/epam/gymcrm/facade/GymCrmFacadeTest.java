package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
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
    void shouldDelegateCreateTrainee() {
        Trainee expected = mock(Trainee.class);
        LocalDate dateOfBirth = LocalDate.of(1995, 4, 12);
        when(traineeService.createTrainee("Juan", "Perez", dateOfBirth, "Address"))
                .thenReturn(expected);

        Trainee result = facade.createTrainee("Juan", "Perez", dateOfBirth, "Address");

        assertSame(expected, result);
        verify(traineeService).createTrainee("Juan", "Perez", dateOfBirth, "Address");
    }

    @Test
    void shouldDelegateCreateTraining() {
        Training expected = mock(Training.class);
        TrainingType fitness = new TrainingType("FITNESS");
        LocalDate date = LocalDate.of(2026, 10, 5);
        when(trainingService.createTraining(
                100L, 200L, "Morning Fitness", fitness, date, 60))
                .thenReturn(expected);

        Training result = facade.createTraining(
                100L, 200L, "Morning Fitness", fitness, date, 60);

        assertSame(expected, result);
        verify(trainingService).createTraining(
                100L, 200L, "Morning Fitness", fitness, date, 60);
    }
}
