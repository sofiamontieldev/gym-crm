package com.epam.gymcrm.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component("gymDatabase")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                return Health.down()
                        .withDetail("reason", "Database connection validation failed")
                        .build();
            }

            return Health.up()
                    .withDetail(
                            "database",
                            connection.getMetaData().getDatabaseProductName())
                    .build();
        } catch (SQLException exception) {
            log.warn("Database health check failed", exception);
            return Health.down(exception).build();
        }
    }
}