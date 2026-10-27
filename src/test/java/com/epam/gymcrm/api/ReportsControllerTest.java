package com.epam.gymcrm.api;

import com.epam.gymcrm.integration.reports.ReportsClient;
import com.epam.gymcrm.integration.reports.TrainerStatus;
import com.epam.gymcrm.integration.reports.TrainerWorkloadResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportsControllerTest {

    @Test
    void delegatesValidFiltersAndRejectsMonthWithoutYear() {
        ReportsClient client = mock(ReportsClient.class);
        ReportsController controller = new ReportsController(client);
        TrainerWorkloadResponse expected = new TrainerWorkloadResponse(
                "Andres.Gomez",
                "Andres",
                "Gomez",
                TrainerStatus.ACTIVE,
                "MINUTES",
                List.of(new TrainerWorkloadResponse.WorkloadPeriodResponse(2026, 10, 60)));
        when(client.findWorkload("Andres.Gomez", 2026, 10)).thenReturn(expected);

        assertEquals(expected, controller.findWorkload("Andres.Gomez", 2026, 10));
        verify(client).findWorkload("Andres.Gomez", 2026, 10);
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.findWorkload("Andres.Gomez", null, 10));
    }
}
