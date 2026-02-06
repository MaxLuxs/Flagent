package com.flagent.sample;

import com.flagent.client.ApiException;
import com.flagent.client.api.EvaluationApi;
import com.flagent.client.model.EvalContext;
import com.flagent.client.model.EvalResult;
import com.flagent.client.model.EvaluationBatchRequest;
import com.flagent.client.model.EvaluationBatchResponse;
import com.flagent.client.model.EvaluationEntity;
import com.flagent.spring.boot.flagent.FlagentEvaluationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Example controller demonstrating single evaluation, batch evaluation, and entityContext.
 */
@RestController
public class EvalController {

    private final FlagentEvaluationFacade flagentFacade;
    private final EvaluationApi evaluationApi;

    public EvalController(FlagentEvaluationFacade flagentEvaluationFacade, EvaluationApi evaluationApi) {
        this.flagentFacade = flagentEvaluationFacade;
        this.evaluationApi = evaluationApi;
    }

    @GetMapping("/eval")
    public ResponseEntity<EvalResult> eval(
            @RequestParam(defaultValue = "my_flag") String flagKey,
            @RequestParam(defaultValue = "user-1") String entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String tier) throws ApiException {
        Map<String, Object> entityContext = null;
        if (country != null || tier != null) {
            entityContext = new java.util.HashMap<>();
            if (country != null) entityContext.put("country", country);
            if (tier != null) entityContext.put("tier", tier);
        }
        EvalContext context = new EvalContext()
                .flagKey(flagKey)
                .entityID(entityId)
                .entityType(entityType != null ? entityType : "user")
                .entityContext(entityContext);
        EvalResult result = flagentFacade.evaluate(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/eval/batch")
    public ResponseEntity<EvaluationBatchResponse> evalBatch(
            @RequestBody EvaluationBatchRequest request) throws ApiException {
        EvaluationBatchResponse response = evaluationApi.postEvaluationBatch(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Convenience endpoint: batch evaluate with entityContext for constraint-based targeting.
     */
    @GetMapping("/eval/batch-demo")
    public ResponseEntity<EvaluationBatchResponse> evalBatchDemo(
            @RequestParam(defaultValue = "my_flag") String flagKey) throws ApiException {
        EvaluationBatchRequest request = new EvaluationBatchRequest()
                .entities(List.of(
                        new EvaluationEntity()
                                .entityID("user1")
                                .entityType("user")
                                .entityContext(Map.of("country", "US", "tier", "premium")),
                        new EvaluationEntity()
                                .entityID("user2")
                                .entityType("user")
                                .entityContext(Map.of("country", "EU", "tier", "basic"))
                ))
                .flagKeys(List.of(flagKey));
        EvaluationBatchResponse response = evaluationApi.postEvaluationBatch(request);
        return ResponseEntity.ok(response);
    }
}
