package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.integration.reports.TrainerWorkloadEventEmitter;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserDAO userDAO;
    private TrainerDAO trainerDAO;
    private TrainerWorkloadEventEmitter workloadEventEmitter;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        trainerDAO = mock(TrainerDAO.class);
        workloadEventEmitter = mock(TrainerWorkloadEventEmitter.class);
        userService = new UserService(userDAO, trainerDAO, workloadEventEmitter);
    }

    @Test
    void emitsStatusChangeWhenTrainerIsDeactivated() {
        User user = user("Andres.Gomez", UserRole.TRAINER);
        Trainer trainer = new Trainer(user, new TrainingType("FITNESS"));
        when(userDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(user));
        when(trainerDAO.findByUsername("Andres.Gomez")).thenReturn(Optional.of(trainer));

        User updated = userService.changeStatus("Andres.Gomez", false);

        assertFalse(updated.isActive());
        verify(workloadEventEmitter).trainerStatusChanged(trainer);
    }

    @Test
    void doesNotEmitTrainerEventWhenTraineeStatusChanges() {
        User user = user("Valentina.Rojas", UserRole.TRAINEE);
        when(userDAO.findByUsername("Valentina.Rojas")).thenReturn(Optional.of(user));

        User inactive = userService.changeStatus("Valentina.Rojas", false);
        assertFalse(inactive.isActive());
        User active = userService.changeStatus("Valentina.Rojas", true);

        assertTrue(active.isActive());
        verifyNoInteractions(trainerDAO, workloadEventEmitter);
    }

    private static User user(String username, UserRole role) {
        return new User.Builder()
                .setFirstName("Lucia")
                .setLastName("Herrera")
                .setUsername(username)
                .setPassword("Abc1234567")
                .setRole(role)
                .setActive(true)
                .build();
    }
}
