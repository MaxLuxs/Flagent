package com.flagent.spring.boot.flagent;

import com.flagent.client.api.HealthApi;
import com.flagent.client.model.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * Health indicator that calls Flagent /health endpoint. Registered only when actuator is on classpath (see FlagentHealthIndicatorAutoConfiguration).
 */
public class FlagentHealthIndicator implements HealthIndicator {

    private static final String FLAGENT = "flagent";

    private final HealthApi healthApi;

    public FlagentHealthIndicator(HealthApi healthApi) {
        this.healthApi = healthApi;
    }

    @Override
    public org.springframework.boot.actuate.health.Health health() {
        try {
            Health health = healthApi.getHealth();
            String status = health != null && health.getStatus() != null ? health.getStatus() : "unknown";
            boolean up = "ok".equalsIgnoreCase(status) || "up".equalsIgnoreCase(status);
            return org.springframework.boot.actuate.health.Health
                    .status(up ? Status.UP : Status.DOWN)
                    .withDetail(FLAGENT, status)
                    .build();
        } catch (Exception e) {
            return org.springframework.boot.actuate.health.Health
                    .down()
                    .withDetail(FLAGENT, e.getMessage())
                    .build();
        }
    }
}
