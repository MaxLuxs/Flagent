package com.flagent.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvaluationBatchRequest
 */
class EvaluationBatchRequestTest {

    @Test
    void testEvaluationBatchRequest() {
        List<EvaluationEntity> entities = Arrays.asList(
            new EvaluationEntity().entityID("e1").entityType("user")
        );
        EvaluationBatchRequest model = new EvaluationBatchRequest()
            .entities(entities)
            .enableDebug(true)
            .flagIDs(Arrays.asList(1, 2))
            .flagKeys(Arrays.asList("f1", "f2"))
            .flagTags(Arrays.asList("prod"))
            .flagTagsOperator(EvaluationBatchRequest.FlagTagsOperatorEnum.ALL);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1, model.getEntities().size());
        Assertions.assertEquals("e1", model.getEntities().get(0).getEntityID());
        Assertions.assertTrue(model.getEnableDebug());
        Assertions.assertEquals(2, model.getFlagIDs().size());
        Assertions.assertEquals(2, model.getFlagKeys().size());
        Assertions.assertEquals(1, model.getFlagTags().size());
        Assertions.assertEquals(EvaluationBatchRequest.FlagTagsOperatorEnum.ALL, model.getFlagTagsOperator());
    }

    @Test
    void entitiesTest() {
        EvaluationEntity e = new EvaluationEntity().entityID("x").entityType("user");
        EvaluationBatchRequest model = new EvaluationBatchRequest().entities(Collections.singletonList(e));
        Assertions.assertEquals(1, model.getEntities().size());
        Assertions.assertEquals("x", model.getEntities().get(0).getEntityID());
    }

    @Test
    void enableDebugTest() {
        EvaluationBatchRequest model = new EvaluationBatchRequest().enableDebug(true);
        Assertions.assertTrue(model.getEnableDebug());
    }

    @Test
    void flagIDsTest() {
        EvaluationBatchRequest model = new EvaluationBatchRequest().flagIDs(Arrays.asList(1, 2, 3));
        Assertions.assertEquals(3, model.getFlagIDs().size());
    }

    @Test
    void flagKeysTest() {
        EvaluationBatchRequest model = new EvaluationBatchRequest().flagKeys(Arrays.asList("a", "b"));
        Assertions.assertEquals(2, model.getFlagKeys().size());
    }

    @Test
    void flagTagsTest() {
        EvaluationBatchRequest model = new EvaluationBatchRequest().flagTags(Arrays.asList("t1"));
        Assertions.assertEquals(1, model.getFlagTags().size());
    }

    @Test
    void flagTagsOperatorTest() {
        EvaluationBatchRequest model = new EvaluationBatchRequest().flagTagsOperator(EvaluationBatchRequest.FlagTagsOperatorEnum.ANY);
        Assertions.assertEquals(EvaluationBatchRequest.FlagTagsOperatorEnum.ANY, model.getFlagTagsOperator());
    }

}
