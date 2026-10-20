package com.epam.gymcrm.monitoring;

import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingCatalogHealthIndicatorTest {

    @Test
    void reportsUpWhenAllTrainingTypesExist() {
        TrainingTypeService service = mock(TrainingTypeService.class);
        when(service.findAll()).thenReturn(TrainingType.FIXED_NAMES.stream()
                .map(TrainingType::new)
                .toList());

        TrainingCatalogHealthIndicator indicator =
                new TrainingCatalogHealthIndicator(service);

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void reportsDownWhenTrainingTypeIsMissing() {
        TrainingTypeService service = mock(TrainingTypeService.class);
        when(service.findAll()).thenReturn(List.of(new TrainingType("YOGA")));

        TrainingCatalogHealthIndicator indicator =
                new TrainingCatalogHealthIndicator(service);

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
