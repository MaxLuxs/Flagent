package com.flagent.spring.boot.flagent;

import com.flagent.client.api.HealthApi;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Registers FlagentHealthIndicator when actuator is on classpath.
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnBean(HealthApi.class)
@ConditionalOnProperty(prefix = "flagent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FlagentHealthIndicatorAutoConfiguration {

    @Bean
    public FlagentHealthIndicator flagentHealthIndicator(HealthApi flagentHealthApi) {
        return new FlagentHealthIndicator(flagentHealthApi);
    }
}
