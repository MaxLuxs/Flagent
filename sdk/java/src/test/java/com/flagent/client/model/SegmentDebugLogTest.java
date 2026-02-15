package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for SegmentDebugLog
 */
class SegmentDebugLogTest {

    @Test
    void testSegmentDebugLog() {
        SegmentDebugLog model = new SegmentDebugLog().segmentID(1L).msg("Segment evaluated");
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getSegmentID());
        Assertions.assertEquals("Segment evaluated", model.getMsg());
    }

    @Test
    void segmentIDTest() {
        SegmentDebugLog model = new SegmentDebugLog().segmentID(10L);
        Assertions.assertEquals(10L, model.getSegmentID());
    }

    @Test
    void msgTest() {
        SegmentDebugLog model = new SegmentDebugLog().msg("Debug message");
        Assertions.assertEquals("Debug message", model.getMsg());
    }

}
