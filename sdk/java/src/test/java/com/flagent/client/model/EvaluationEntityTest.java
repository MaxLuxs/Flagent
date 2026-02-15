package com.flagent.client.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvaluationEntity
 */
class EvaluationEntityTest {

    @Test
    void testEvaluationEntity() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("region", "EU");
        EvaluationEntity model = new EvaluationEntity()
            .entityID("user-1")
            .entityType("user")
            .entityContext(ctx);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("user-1", model.getEntityID());
        Assertions.assertEquals("user", model.getEntityType());
        Assertions.assertEquals("EU", model.getEntityContext().get("region"));
    }

    @Test
    void entityIDTest() {
        EvaluationEntity model = new EvaluationEntity().entityID("e1");
        Assertions.assertEquals("e1", model.getEntityID());
    }

    @Test
    void entityTypeTest() {
        EvaluationEntity model = new EvaluationEntity().entityType("device");
        Assertions.assertEquals("device", model.getEntityType());
    }

    @Test
    void entityContextTest() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("k", "v");
        EvaluationEntity model = new EvaluationEntity().entityContext(ctx);
        Assertions.assertEquals("v", model.getEntityContext().get("k"));
    }

}
