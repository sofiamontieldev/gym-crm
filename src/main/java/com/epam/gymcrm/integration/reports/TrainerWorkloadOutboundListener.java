package com.epam.gymcrm.integration.reports;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class TrainerWorkloadOutboundListener {

    private final TrainerWorkloadMessagePublisher publisher;

    public TrainerWorkloadOutboundListener(TrainerWorkloadMessagePublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(TrainerWorkloadEvent event) {
        publisher.publish(event);
    }
}
