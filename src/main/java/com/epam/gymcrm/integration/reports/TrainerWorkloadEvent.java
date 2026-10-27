package com.epam.gymcrm.integration.reports;

import java.time.LocalDate;
import java.util.UUID;

public record TrainerWorkloadEvent(
        UUID eventId,
        EventType eventType,
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        TrainerStatus trainerStatus,
        LocalDate trainingDate,
        Integer trainingDuration) {
}
