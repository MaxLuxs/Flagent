package com.flagent.spring.boot.flagent;

import com.flagent.client.ApiClient;
import com.flagent.client.api.EvaluationApi;
import com.flagent.client.api.HealthApi;
import com.flagent.client.model.EvalResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import java.time.Duration;

/**
 * Auto-configuration for Flagent Java client. Registers ApiClient, EvaluationApi, HealthApi, optional cache, and FlagentEvaluationFacade.
 */
@AutoConfiguration
@ConditionalOnClass(ApiClient.class)
@ConditionalOnProperty(prefix = "flagent", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlagentProperties.class)
public class FlagentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiClient flagentApiClient(FlagentProperties properties) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:18000";
        }
        baseUrl = baseUrl.replaceAll("/+$", "");
        if (!baseUrl.endsWith("/api/v1")) {
            baseUrl = baseUrl + "/api/v1";
        }
        ApiClient client = new ApiClient();
        client.updateBaseUri(baseUrl);
        client.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        client.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public EvaluationApi flagentEvaluationApi(ApiClient flagentApiClient) {
        return new EvaluationApi(flagentApiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public HealthApi flagentHealthApi(ApiClient flagentApiClient) {
        return new HealthApi(flagentApiClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "flagentEvalCache")
    @ConditionalOnProperty(prefix = "flagent.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Cache<String, EvalResult> flagentEvalCache(FlagentProperties properties) {
        long ttlMs = properties.getCache().getTtlMs() > 0 ? properties.getCache().getTtlMs() : 60000;
        return Caffeine.newBuilder()
                .expireAfterWrite(ttlMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlagentEvaluationFacade flagentEvaluationFacade(EvaluationApi flagentEvaluationApi,
                                                          FlagentProperties properties,
                                                          @org.springframework.beans.factory.annotation.Autowired(required = false) Cache<String, EvalResult> flagentEvalCache) {
        if (flagentEvalCache != null && properties.getCache().isEnabled()) {
            return new FlagentEvaluationFacade(flagentEvaluationApi, flagentEvalCache);
        }
        return new FlagentEvaluationFacade(flagentEvaluationApi);
    }
}
