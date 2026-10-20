package com.epam.gymcrm.monitoring;

import com.epam.gymcrm.model.UserRole;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GymMetrics {

    private final MeterRegistry meterRegistry;

    public GymMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void profileRegistered(UserRole role) {
        meterRegistry.counter(
                        "gym.profile.registrations",
                        "role",
                        roleTag(role))
                .increment();
    }

    public void trainingCreated(String trainingType) {
        meterRegistry.counter(
                        "gym.trainings.created",
                        "type",
                        trainingType)
                .increment();
    }

    public void passwordChanged(UserRole role) {
        meterRegistry.counter(
                        "gym.password.changes",
                        "role",
                        roleTag(role))
                .increment();
    }

    private static String roleTag(UserRole role) {
        return role.name().toLowerCase(Locale.ROOT);
    }
}
