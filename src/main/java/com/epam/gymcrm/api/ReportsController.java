package com.epam.gymcrm.api;

import com.epam.gymcrm.integration.reports.ReportsClient;
import com.epam.gymcrm.integration.reports.TrainerWorkloadResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
@ConditionalOnProperty(name = "gym.integrations.reports.enabled", havingValue = "true")
public class ReportsController {

    private final ReportsClient reportsClient;

    public ReportsController(ReportsClient reportsClient) {
        this.reportsClient = reportsClient;
    }

    @GetMapping("/trainers/{trainerUsername}/workload")
    public TrainerWorkloadResponse findWorkload(
            @PathVariable String trainerUsername,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {

        if (month != null && year == null) {
            throw new IllegalArgumentException("year is required when month is provided");
        }
        return reportsClient.findWorkload(trainerUsername, year, month);
    }
}
