package com.epam.gymcrm.monitoring;

import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component("trainingCatalog")
public class TrainingCatalogHealthIndicator implements HealthIndicator {

    private static final Logger log =
            LoggerFactory.getLogger(TrainingCatalogHealthIndicator.class);

    private final TrainingTypeService trainingTypeService;

    public TrainingCatalogHealthIndicator(
            TrainingTypeService trainingTypeService) {

        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public Health health() {
        try {
            Set<String> actualTypes = trainingTypeService.findAll().stream()
                    .map(TrainingType::getName)
                    .collect(Collectors.toSet());

            if (actualTypes.equals(TrainingType.FIXED_NAMES)) {
                return Health.up()
                        .withDetail("configuredTypes", actualTypes.size())
                        .build();
            }

            return Health.down()
                    .withDetail("expectedTypes", TrainingType.FIXED_NAMES)
                    .withDetail("actualTypes", actualTypes)
                    .build();
        } catch (RuntimeException exception) {
            log.warn("Training catalog health check failed", exception);
            return Health.down(exception).build();
        }
    }
}