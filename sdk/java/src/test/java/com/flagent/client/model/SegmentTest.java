package com.flagent.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Segment
 */
class SegmentTest {

    @Test
    void testSegment() {
        Segment model = new Segment()
            .id(1L)
            .flagID(10L)
            .description("Segment 1")
            .rank(0L)
            .rolloutPercent(100L);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals(10L, model.getFlagID());
        Assertions.assertEquals("Segment 1", model.getDescription());
    }

    @Test
    void idTest() {
        Segment model = new Segment().id(5L);
        Assertions.assertEquals(5L, model.getId());
    }

    @Test
    void flagIDTest() {
        Segment model = new Segment().flagID(20L);
        Assertions.assertEquals(20L, model.getFlagID());
    }

    @Test
    void descriptionTest() {
        Segment model = new Segment().description("Desc");
        Assertions.assertEquals("Desc", model.getDescription());
    }

    @Test
    void rankTest() {
        Segment model = new Segment().rank(1L);
        Assertions.assertEquals(1L, model.getRank());
    }

    @Test
    void rolloutPercentTest() {
        Segment model = new Segment().rolloutPercent(50L);
        Assertions.assertEquals(50L, model.getRolloutPercent());
    }

    @Test
    void constraintsTest() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint().id(1L).segmentID(1L).property("region").operator(Constraint.OperatorEnum.EQ).value("EU"));
        Segment model = new Segment().constraints(constraints);
        Assertions.assertNotNull(model.getConstraints());
        Assertions.assertEquals(1, model.getConstraints().size());
        Assertions.assertEquals("region", model.getConstraints().get(0).getProperty());
    }

    @Test
    void distributionsTest() {
        List<Distribution> distributions = new ArrayList<>();
        distributions.add(new Distribution().id(1L).segmentID(1L).variantID(1L).variantKey("control").percent(100L));
        Segment model = new Segment().distributions(distributions);
        Assertions.assertNotNull(model.getDistributions());
        Assertions.assertEquals(1, model.getDistributions().size());
        Assertions.assertEquals(100L, model.getDistributions().get(0).getPercent());
    }

}
