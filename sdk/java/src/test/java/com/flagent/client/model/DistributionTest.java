package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Distribution
 */
class DistributionTest {

    @Test
    void testDistribution() {
        Distribution model = new Distribution()
            .id(1L)
            .segmentID(10L)
            .variantID(2L)
            .variantKey("control")
            .percent(100L);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals(10L, model.getSegmentID());
        Assertions.assertEquals(2L, model.getVariantID());
        Assertions.assertEquals(100L, model.getPercent());
    }

    @Test
    void idTest() {
        Distribution model = new Distribution().id(5L);
        Assertions.assertEquals(5L, model.getId());
    }

    @Test
    void segmentIDTest() {
        Distribution model = new Distribution().segmentID(20L);
        Assertions.assertEquals(20L, model.getSegmentID());
    }

    @Test
    void variantIDTest() {
        Distribution model = new Distribution().variantID(3L);
        Assertions.assertEquals(3L, model.getVariantID());
    }

    @Test
    void variantKeyTest() {
        Distribution model = new Distribution().variantKey("variant_a");
        Assertions.assertEquals("variant_a", model.getVariantKey());
    }

    @Test
    void percentTest() {
        Distribution model = new Distribution().percent(50L);
        Assertions.assertEquals(50L, model.getPercent());
    }

}
