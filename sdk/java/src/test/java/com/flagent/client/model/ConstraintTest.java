package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Constraint
 */
class ConstraintTest {

    @Test
    void testConstraint() {
        Constraint model = new Constraint()
            .id(1L)
            .segmentID(10L)
            .property("region")
            .operator(Constraint.OperatorEnum.IN)
            .value("EU,US");
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals(10L, model.getSegmentID());
        Assertions.assertEquals("region", model.getProperty());
        Assertions.assertEquals(Constraint.OperatorEnum.IN, model.getOperator());
        Assertions.assertEquals("EU,US", model.getValue());
    }

    @Test
    void idTest() {
        Constraint model = new Constraint().id(5L);
        Assertions.assertEquals(5L, model.getId());
    }

    @Test
    void segmentIDTest() {
        Constraint model = new Constraint().segmentID(20L);
        Assertions.assertEquals(20L, model.getSegmentID());
    }

    @Test
    void propertyTest() {
        Constraint model = new Constraint().property("tier");
        Assertions.assertEquals("tier", model.getProperty());
    }

    @Test
    void operatorTest() {
        Constraint model = new Constraint().operator(Constraint.OperatorEnum.EQ);
        Assertions.assertEquals(Constraint.OperatorEnum.EQ, model.getOperator());
    }

    @Test
    void valueTest() {
        Constraint model = new Constraint().value("premium");
        Assertions.assertEquals("premium", model.getValue());
    }

}
