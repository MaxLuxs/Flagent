package com.flagent.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for EvalDebugLog
 */
class EvalDebugLogTest {

    @Test
    void testEvalDebugLog() {
        SegmentDebugLog segLog = new SegmentDebugLog().segmentID(1L).msg("segment matched");
        List<SegmentDebugLog> segmentLogs = Collections.singletonList(segLog);
        EvalDebugLog model = new EvalDebugLog()
            .msg("Evaluation completed")
            .segmentDebugLogs(segmentLogs);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("Evaluation completed", model.getMsg());
        Assertions.assertEquals(1, model.getSegmentDebugLogs().size());
        Assertions.assertEquals("segment matched", model.getSegmentDebugLogs().get(0).getMsg());
    }

    @Test
    void msgTest() {
        EvalDebugLog model = new EvalDebugLog().msg("message");
        Assertions.assertEquals("message", model.getMsg());
    }

    @Test
    void segmentDebugLogsTest() {
        List<SegmentDebugLog> logs = new ArrayList<>();
        logs.add(new SegmentDebugLog().segmentID(1L).msg("m1"));
        EvalDebugLog model = new EvalDebugLog().segmentDebugLogs(logs);
        Assertions.assertEquals(1, model.getSegmentDebugLogs().size());
        Assertions.assertEquals(1L, model.getSegmentDebugLogs().get(0).getSegmentID());
    }

}
