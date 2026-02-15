package com.flagent.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvalContext
 */
class EvalContextTest {

    @Test
    void testEvalContext() {
        EvalContext model = new EvalContext()
            .entityID("user-1")
            .entityType("user")
            .entityContext(new HashMap<>(Map.of("region", "EU")))
            .enableDebug(true)
            .flagID(1L)
            .flagKey("my_flag")
            .flagTags(Arrays.asList("prod"))
            .flagTagsOperator(EvalContext.FlagTagsOperatorEnum.ANY);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("user-1", model.getEntityID());
        Assertions.assertEquals("user", model.getEntityType());
        Assertions.assertEquals("EU", model.getEntityContext().get("region"));
        Assertions.assertTrue(model.getEnableDebug());
        Assertions.assertEquals(1L, model.getFlagID());
        Assertions.assertEquals("my_flag", model.getFlagKey());
        Assertions.assertNotNull(model.getFlagTags());
        Assertions.assertEquals(1, model.getFlagTags().size());
        Assertions.assertEquals(EvalContext.FlagTagsOperatorEnum.ANY, model.getFlagTagsOperator());
    }

    @Test
    void entityIDTest() {
        EvalContext model = new EvalContext().entityID("e1");
        Assertions.assertEquals("e1", model.getEntityID());
    }

    @Test
    void entityTypeTest() {
        EvalContext model = new EvalContext().entityType("device");
        Assertions.assertEquals("device", model.getEntityType());
    }

    @Test
    void entityContextTest() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("key", "value");
        EvalContext model = new EvalContext().entityContext(ctx);
        Assertions.assertEquals("value", model.getEntityContext().get("key"));
    }

    @Test
    void enableDebugTest() {
        EvalContext model = new EvalContext().enableDebug(true);
        Assertions.assertTrue(model.getEnableDebug());
    }

    @Test
    void flagIDTest() {
        EvalContext model = new EvalContext().flagID(10L);
        Assertions.assertEquals(10L, model.getFlagID());
    }

    @Test
    void flagKeyTest() {
        EvalContext model = new EvalContext().flagKey("flag_key");
        Assertions.assertEquals("flag_key", model.getFlagKey());
    }

    @Test
    void flagTagsTest() {
        List<String> tags = Arrays.asList("a", "b");
        EvalContext model = new EvalContext().flagTags(tags);
        Assertions.assertEquals(2, model.getFlagTags().size());
    }

    @Test
    void flagTagsOperatorTest() {
        EvalContext model = new EvalContext().flagTagsOperator(EvalContext.FlagTagsOperatorEnum.ALL);
        Assertions.assertEquals(EvalContext.FlagTagsOperatorEnum.ALL, model.getFlagTagsOperator());
    }

}
