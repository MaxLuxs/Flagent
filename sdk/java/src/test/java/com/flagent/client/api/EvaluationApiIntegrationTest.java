/*
 * Integration tests for EvaluationApi. Run against a live Flagent backend.
 *
 * To run: set FLAGENT_BASE_URL (e.g. http://localhost:18000/api/v1), start backend
 * (./gradlew :backend:runDev), optionally seed data (./scripts/seed-demo-data.sh), then:
 * FLAGENT_BASE_URL=http://localhost:18000/api/v1 ./gradlew :sdk:java:test --tests "*EvaluationApiIntegrationTest"
 */

package com.flagent.client.api;

import com.flagent.client.ApiClient;
import com.flagent.client.ApiException;
import com.flagent.client.Configuration;
import com.flagent.client.model.EvalContext;
import com.flagent.client.model.EvalResult;
import com.flagent.client.model.EvaluationBatchRequest;
import com.flagent.client.model.EvaluationBatchResponse;
import com.flagent.client.model.EvaluationEntity;
import com.flagent.client.model.Health;
import com.flagent.client.model.Info;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

@EnabledIfEnvironmentVariable(named = "FLAGENT_BASE_URL", matches = ".+")
class EvaluationApiIntegrationTest {

    private static String baseUrl;
    private static EvaluationApi evaluationApi;
    private static HealthApi healthApi;

    @BeforeAll
    static void setup() {
        baseUrl = System.getenv("FLAGENT_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            return;
        }
        ApiClient client = new ApiClient();
        client.updateBaseUri(baseUrl);
        Configuration.setDefaultApiClient(client);
        evaluationApi = new EvaluationApi(client);
        healthApi = new HealthApi(client);
    }

    @Test
    void healthCheck() throws ApiException {
        Health health = healthApi.getHealth();
        Assertions.assertNotNull(health);
        Assertions.assertNotNull(health.getStatus());
    }

    @Test
    void getInfo() throws ApiException {
        Info info = healthApi.getInfo();
        Assertions.assertNotNull(info);
    }

    @Test
    void postEvaluation_withFlagKeyAndEntity() throws ApiException {
        EvalContext ctx = new EvalContext()
                .flagKey("my_feature_flag")
                .entityID("user-integration-test")
                .entityType("user");
        EvalResult result = evaluationApi.postEvaluation(ctx);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("my_feature_flag", result.getFlagKey());
        Assertions.assertNotNull(result.getVariantKey());
    }

    @Test
    void postEvaluationBatch_withFlagKeysAndEntities() throws ApiException {
        EvaluationEntity entity = new EvaluationEntity()
                .entityID("batch-user-1")
                .entityType("user");
        EvaluationBatchRequest request = new EvaluationBatchRequest()
                .flagKeys(List.of("my_feature_flag"))
                .entities(List.of(entity));
        EvaluationBatchResponse response = evaluationApi.postEvaluationBatch(request);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getEvaluationResults());
        // Backend may return empty if flag does not exist, or results if it does
        if (!response.getEvaluationResults().isEmpty()) {
            EvalResult first = response.getEvaluationResults().get(0);
            Assertions.assertNotNull(first.getFlagKey());
            Assertions.assertNotNull(first.getVariantKey());
        }
    }
}
