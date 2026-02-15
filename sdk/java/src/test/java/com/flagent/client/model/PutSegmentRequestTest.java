package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutSegmentRequest
 */
class PutSegmentRequestTest {

    @Test
    void testPutSegmentRequest() {
        PutSegmentRequest model = new PutSegmentRequest()
            .description("Updated segment")
            .rolloutPercent(75L);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("Updated segment", model.getDescription());
        Assertions.assertEquals(75L, model.getRolloutPercent());
    }

    @Test
    void descriptionTest() {
        PutSegmentRequest model = new PutSegmentRequest().description("Desc");
        Assertions.assertEquals("Desc", model.getDescription());
    }

    @Test
    void rolloutPercentTest() {
        PutSegmentRequest model = new PutSegmentRequest().rolloutPercent(50L);
        Assertions.assertEquals(50L, model.getRolloutPercent());
    }

}
