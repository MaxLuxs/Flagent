package com.flagent.client.model;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvalResult
 */
class EvalResultTest {

    @Test
    void testEvalResult() {
        EvalContext ctx = new EvalContext().entityID("e1").entityType("user");
        EvalDebugLog debugLog = new EvalDebugLog().msg("ok");
        OffsetDateTime ts = OffsetDateTime.now();
        EvalResult model = new EvalResult()
            .flagID(1L)
            .flagKey("my_flag")
            .flagSnapshotID(10L)
            .flagTags(Arrays.asList("prod"))
            .segmentID(2L)
            .variantID(3L)
            .variantKey("control")
            .variantAttachment(new HashMap<>(Map.of("value", true)))
            .evalContext(ctx)
            .timestamp(ts)
            .evalDebugLog(debugLog);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getFlagID());
        Assertions.assertEquals("my_flag", model.getFlagKey());
        Assertions.assertEquals(10L, model.getFlagSnapshotID());
        Assertions.assertEquals(1, model.getFlagTags().size());
        Assertions.assertEquals(2L, model.getSegmentID());
        Assertions.assertEquals(3L, model.getVariantID());
        Assertions.assertEquals("control", model.getVariantKey());
        Assertions.assertEquals(true, model.getVariantAttachment().get("value"));
        Assertions.assertEquals(ctx, model.getEvalContext());
        Assertions.assertEquals(ts, model.getTimestamp());
        Assertions.assertEquals(debugLog, model.getEvalDebugLog());
    }

    @Test
    void flagIDTest() {
        EvalResult model = new EvalResult().flagID(5L);
        Assertions.assertEquals(5L, model.getFlagID());
    }

    @Test
    void flagKeyTest() {
        EvalResult model = new EvalResult().flagKey("key");
        Assertions.assertEquals("key", model.getFlagKey());
    }

    @Test
    void flagSnapshotIDTest() {
        EvalResult model = new EvalResult().flagSnapshotID(100L);
        Assertions.assertEquals(100L, model.getFlagSnapshotID());
    }

    @Test
    void flagTagsTest() {
        EvalResult model = new EvalResult().flagTags(Arrays.asList("a", "b"));
        Assertions.assertEquals(2, model.getFlagTags().size());
    }

    @Test
    void segmentIDTest() {
        EvalResult model = new EvalResult().segmentID(1L);
        Assertions.assertEquals(1L, model.getSegmentID());
    }

    @Test
    void variantIDTest() {
        EvalResult model = new EvalResult().variantID(2L);
        Assertions.assertEquals(2L, model.getVariantID());
    }

    @Test
    void variantKeyTest() {
        EvalResult model = new EvalResult().variantKey("variant_a");
        Assertions.assertEquals("variant_a", model.getVariantKey());
    }

    @Test
    void variantAttachmentTest() {
        Map<String, Object> att = new HashMap<>();
        att.put("x", 1);
        EvalResult model = new EvalResult().variantAttachment(att);
        Assertions.assertEquals(1, model.getVariantAttachment().get("x"));
    }

    @Test
    void evalContextTest() {
        EvalContext ctx = new EvalContext().entityID("e1");
        EvalResult model = new EvalResult().evalContext(ctx);
        Assertions.assertEquals("e1", model.getEvalContext().getEntityID());
    }

    @Test
    void timestampTest() {
        OffsetDateTime ts = OffsetDateTime.now();
        EvalResult model = new EvalResult().timestamp(ts);
        Assertions.assertEquals(ts, model.getTimestamp());
    }

    @Test
    void evalDebugLogTest() {
        EvalDebugLog log = new EvalDebugLog().msg("debug");
        EvalResult model = new EvalResult().evalDebugLog(log);
        Assertions.assertEquals("debug", model.getEvalDebugLog().getMsg());
    }

}
