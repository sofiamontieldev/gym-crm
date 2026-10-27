package com.epam.gymcrm.config;

import com.epam.gymcrm.integration.reports.TrainerWorkloadEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class ActiveMqConfig {

    @Bean
    public MessageConverter trainerWorkloadMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of("trainer-workload-v1", TrainerWorkloadEvent.class));
        return converter;
    }
}
