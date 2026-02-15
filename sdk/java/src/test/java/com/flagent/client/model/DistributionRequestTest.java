package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for DistributionRequest
 */
class DistributionRequestTest {

    @Test
    void testDistributionRequest() {
        DistributionRequest model = new DistributionRequest()
            .variantID(1L)
            .variantKey("control")
            .percent(100L);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getVariantID());
        Assertions.assertEquals("control", model.getVariantKey());
        Assertions.assertEquals(100L, model.getPercent());
    }

    @Test
    void variantIDTest() {
        DistributionRequest model = new DistributionRequest().variantID(5L);
        Assertions.assertEquals(5L, model.getVariantID());
    }

    @Test
    void variantKeyTest() {
        DistributionRequest model = new DistributionRequest().variantKey("variant_a");
        Assertions.assertEquals("variant_a", model.getVariantKey());
    }

    @Test
    void percentTest() {
        DistributionRequest model = new DistributionRequest().percent(50L);
        Assertions.assertEquals(50L, model.getPercent());
    }

}
