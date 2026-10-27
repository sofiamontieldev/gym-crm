package com.epam.gymcrm.integration.reports;

public interface TrainerWorkloadMessagePublisher {

    void publish(TrainerWorkloadEvent event);
}
