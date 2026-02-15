package com.flagent.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvaluationBatchResponse
 */
class EvaluationBatchResponseTest {

    @Test
    void testEvaluationBatchResponse() {
        EvalResult r = new EvalResult().flagID(1L).flagKey("f1").variantKey("control");
        List<EvalResult> results = Collections.singletonList(r);
        EvaluationBatchResponse model = new EvaluationBatchResponse().evaluationResults(results);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1, model.getEvaluationResults().size());
        Assertions.assertEquals(1L, model.getEvaluationResults().get(0).getFlagID());
        Assertions.assertEquals("f1", model.getEvaluationResults().get(0).getFlagKey());
    }

    @Test
    void evaluationResultsTest() {
        List<EvalResult> results = Arrays.asList(
            new EvalResult().flagKey("a"),
            new EvalResult().flagKey("b")
        );
        EvaluationBatchResponse model = new EvaluationBatchResponse().evaluationResults(results);
        Assertions.assertEquals(2, model.getEvaluationResults().size());
    }

}
