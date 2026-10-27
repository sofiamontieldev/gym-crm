package com.epam.gymcrm.integration.reports;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TrainerWorkloadEventEmitterTest {

    @Test
    void createsTrainingAndStatusEventsWithTheSharedContract() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        TrainerWorkloadEventEmitter emitter = new TrainerWorkloadEventEmitter(publisher);
        Trainer trainer = trainer();

        emitter.trainingAdded(trainer, LocalDate.of(2026, 10, 20), 60);
        trainer.getUser().deactivate();
        emitter.trainerStatusChanged(trainer);

        ArgumentCaptor<TrainerWorkloadEvent> captor =
                ArgumentCaptor.forClass(TrainerWorkloadEvent.class);
        verify(publisher, org.mockito.Mockito.times(2)).publishEvent(captor.capture());

        TrainerWorkloadEvent training = captor.getAllValues().get(0);
        assertNotNull(training.eventId());
        assertEquals(EventType.TRAINING_ADDED, training.eventType());
        assertEquals("Andres.Gomez", training.trainerUsername());
        assertEquals(TrainerStatus.ACTIVE, training.trainerStatus());
        assertEquals(LocalDate.of(2026, 10, 20), training.trainingDate());
        assertEquals(60, training.trainingDuration());

        TrainerWorkloadEvent status = captor.getAllValues().get(1);
        assertEquals(EventType.TRAINER_STATUS_CHANGED, status.eventType());
        assertEquals(TrainerStatus.INACTIVE, status.trainerStatus());
        assertNull(status.trainingDate());
        assertNull(status.trainingDuration());
    }

    private static Trainer trainer() {
        User user = new User.Builder()
                .setFirstName("Andres")
                .setLastName("Gomez")
                .setUsername("Andres.Gomez")
                .setPassword("Abc1234567")
                .setActive(true)
                .build();
        return new Trainer(user, new TrainingType("FITNESS"));
    }
}
