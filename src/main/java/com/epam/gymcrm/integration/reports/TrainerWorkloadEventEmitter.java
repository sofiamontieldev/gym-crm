package com.epam.gymcrm.integration.reports;

import com.epam.gymcrm.model.Trainer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class TrainerWorkloadEventEmitter {

    private final ApplicationEventPublisher eventPublisher;

    public TrainerWorkloadEventEmitter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void trainingAdded(Trainer trainer, LocalDate trainingDate, int trainingDuration) {
        eventPublisher.publishEvent(event(
                EventType.TRAINING_ADDED,
                trainer,
                trainingDate,
                trainingDuration));
    }

    public void trainerStatusChanged(Trainer trainer) {
        eventPublisher.publishEvent(event(
                EventType.TRAINER_STATUS_CHANGED,
                trainer,
                null,
                null));
    }

    private static TrainerWorkloadEvent event(
            EventType eventType,
            Trainer trainer,
            LocalDate trainingDate,
            Integer trainingDuration) {

        return new TrainerWorkloadEvent(
                UUID.randomUUID(),
                eventType,
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive() ? TrainerStatus.ACTIVE : TrainerStatus.INACTIVE,
                trainingDate,
                trainingDuration);
    }
}
