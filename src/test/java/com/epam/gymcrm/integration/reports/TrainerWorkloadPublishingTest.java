package com.epam.gymcrm.integration.reports;

import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TrainerWorkloadPublishingTest {

    @Test
    void publishesToConfiguredQueueOnlyWhenAfterCommitListenerRuns() {
        JmsTemplate jmsTemplate = mock(JmsTemplate.class);
        ActiveMqTrainerWorkloadMessagePublisher publisher =
                new ActiveMqTrainerWorkloadMessagePublisher(jmsTemplate, "trainer.workload");
        TrainerWorkloadOutboundListener listener = new TrainerWorkloadOutboundListener(publisher);
        TrainerWorkloadEvent event = new TrainerWorkloadEvent(
                UUID.randomUUID(),
                EventType.TRAINING_ADDED,
                "Andres.Gomez",
                "Andres",
                "Gomez",
                TrainerStatus.ACTIVE,
                LocalDate.of(2026, 10, 20),
                60);

        listener.afterCommit(event);

        verify(jmsTemplate).convertAndSend("trainer.workload", event);
    }
}
