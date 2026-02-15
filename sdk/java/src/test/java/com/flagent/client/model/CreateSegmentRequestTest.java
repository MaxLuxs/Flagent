package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for CreateSegmentRequest
 */
class CreateSegmentRequestTest {

    @Test
    void testCreateSegmentRequest() {
        CreateSegmentRequest model = new CreateSegmentRequest()
            .description("New segment")
            .rolloutPercent(100L);
        Assertions.assertNotNull(model);
        Assertions.assertEquals("New segment", model.getDescription());
        Assertions.assertEquals(100L, model.getRolloutPercent());
    }

    @Test
    void descriptionTest() {
        CreateSegmentRequest model = new CreateSegmentRequest().description("Desc");
        Assertions.assertEquals("Desc", model.getDescription());
    }

    @Test
    void rolloutPercentTest() {
        CreateSegmentRequest model = new CreateSegmentRequest().rolloutPercent(50L);
        Assertions.assertEquals(50L, model.getRolloutPercent());
    }

}
