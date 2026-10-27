package com.epam.gymcrm.integration.reports;

import java.util.List;

public record TrainerWorkloadResponse(
        String trainerUsername,
        String firstName,
        String lastName,
        TrainerStatus status,
        String unit,
        List<WorkloadPeriodResponse> periods) {

    public record WorkloadPeriodResponse(
            int year,
            int month,
            int trainingDuration) {
    }
}
