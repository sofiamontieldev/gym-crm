package com.epam.gymcrm.integration.reports;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "gym.reports")
public record ReportsProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {
}
