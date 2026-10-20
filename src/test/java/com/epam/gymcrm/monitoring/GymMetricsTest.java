package com.epam.gymcrm.monitoring;

import com.epam.gymcrm.model.UserRole;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {

    @Test
    void recordsBusinessMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        GymMetrics metrics = new GymMetrics(registry);

        metrics.profileRegistered(UserRole.TRAINEE);
        metrics.trainingCreated("YOGA");
        metrics.passwordChanged(UserRole.TRAINEE);

        assertEquals(
                1.0,
                registry.get("gym.profile.registrations")
                        .tag("role", "trainee")
                        .counter()
                        .count());
        assertEquals(
                1.0,
                registry.get("gym.trainings.created")
                        .tag("type", "YOGA")
                        .counter()
                        .count());
        assertEquals(
                1.0,
                registry.get("gym.password.changes")
                        .tag("role", "trainee")
                        .counter()
                        .count());
    }
}
