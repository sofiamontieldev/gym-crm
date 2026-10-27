package com.epam.gymcrm.integration.reports;

import com.epam.gymcrm.exception.ReportsUnavailableException;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class ReportsClient {

    private final RestClient restClient;

    public ReportsClient(RestClient reportsRestClient) {
        this.restClient = reportsRestClient;
    }

    public TrainerWorkloadResponse findWorkload(
            String trainerUsername,
            Integer year,
            Integer month) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/reports/trainers/{trainerUsername}/workload");
                        if (year != null) {
                            uriBuilder.queryParam("year", year);
                        }
                        if (month != null) {
                            uriBuilder.queryParam("month", month);
                        }
                        return uriBuilder.build(trainerUsername);
                    })
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResourceNotFoundException(
                                        "Trainer workload not found: " + trainerUsername);
                            })
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new ReportsUnavailableException(
                                        "gym-reports returned HTTP " + response.getStatusCode().value(),
                                        null);
                            })
                    .body(TrainerWorkloadResponse.class);
        } catch (ResourceNotFoundException | ReportsUnavailableException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ReportsUnavailableException("gym-reports is unavailable", exception);
        }
    }
}
