package com.epam.gymcrm.integration.reports;

import com.epam.gymcrm.exception.ReportsUnavailableException;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ReportsClientTest {

    private MockRestServiceServer server;
    private ReportsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://reports:8081");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ReportsClient(builder.build());
    }

    @Test
    void returnsFilteredWorkload() {
        server.expect(once(), requestTo(
                        "http://reports:8081/api/v1/reports/trainers/Andres.Gomez/workload?year=2026&month=10"))
                .andRespond(withSuccess("""
                        {
                          "trainerUsername":"Andres.Gomez",
                          "firstName":"Andres",
                          "lastName":"Gomez",
                          "status":"ACTIVE",
                          "unit":"MINUTES",
                          "periods":[{"year":2026,"month":10,"trainingDuration":60}]
                        }
                        """, MediaType.APPLICATION_JSON));

        TrainerWorkloadResponse response =
                client.findWorkload("Andres.Gomez", 2026, 10);

        assertEquals("Andres.Gomez", response.trainerUsername());
        assertEquals(60, response.periods().getFirst().trainingDuration());
        server.verify();
    }

    @Test
    void translatesNotFoundAndServiceFailures() {
        server.expect(requestTo(
                        "http://reports:8081/api/v1/reports/trainers/Unknown.Trainer/workload"))
                .andRespond(withResourceNotFound());
        assertThrows(
                ResourceNotFoundException.class,
                () -> client.findWorkload("Unknown.Trainer", null, null));

        server.reset();
        server.expect(requestTo(
                        "http://reports:8081/api/v1/reports/trainers/Andres.Gomez/workload"))
                .andRespond(withServerError());
        assertThrows(
                ReportsUnavailableException.class,
                () -> client.findWorkload("Andres.Gomez", null, null));
    }
}
