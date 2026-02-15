package com.flagent.client.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutSegmentReorderRequest
 */
class PutSegmentReorderRequestTest {

    @Test
    void testPutSegmentReorderRequest() {
        List<Long> segmentIDs = Arrays.asList(3L, 1L, 2L);
        PutSegmentReorderRequest model = new PutSegmentReorderRequest().segmentIDs(segmentIDs);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(3, model.getSegmentIDs().size());
        Assertions.assertEquals(3L, model.getSegmentIDs().get(0));
        Assertions.assertEquals(1L, model.getSegmentIDs().get(1));
    }

    @Test
    void segmentIDsTest() {
        List<Long> ids = Arrays.asList(1L, 2L);
        PutSegmentReorderRequest model = new PutSegmentReorderRequest().segmentIDs(ids);
        Assertions.assertEquals(2, model.getSegmentIDs().size());
    }

}
