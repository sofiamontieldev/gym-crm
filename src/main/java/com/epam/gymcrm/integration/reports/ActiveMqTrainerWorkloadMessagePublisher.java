package com.epam.gymcrm.integration.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class ActiveMqTrainerWorkloadMessagePublisher implements TrainerWorkloadMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(
            ActiveMqTrainerWorkloadMessagePublisher.class);

    private final JmsTemplate jmsTemplate;
    private final String destination;

    public ActiveMqTrainerWorkloadMessagePublisher(
            JmsTemplate jmsTemplate,
            @Value("${gym.messaging.trainer-workload-queue}") String destination) {
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
    }

    @Override
    public void publish(TrainerWorkloadEvent event) {
        jmsTemplate.convertAndSend(destination, event);
        log.info("Published {} event {} for trainer {}",
                event.eventType(), event.eventId(), event.trainerUsername());
    }
}
