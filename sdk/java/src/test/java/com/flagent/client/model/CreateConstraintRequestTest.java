package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for CreateConstraintRequest
 */
class CreateConstraintRequestTest {

    @Test
    void testCreateConstraintRequest() {
        CreateConstraintRequest model = new CreateConstraintRequest()
            .property("region")
            .operator("IN")
            .value("EU,US");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("region", model.getProperty());
        Assertions.assertEquals("IN", model.getOperator());
        Assertions.assertEquals("EU,US", model.getValue());
    }

    @Test
    void propertyTest() {
        CreateConstraintRequest model = new CreateConstraintRequest().property("tier");
        Assertions.assertEquals("tier", model.getProperty());
    }

    @Test
    void operatorTest() {
        CreateConstraintRequest model = new CreateConstraintRequest().operator("EQ");
        Assertions.assertEquals("EQ", model.getOperator());
    }

    @Test
    void valueTest() {
        CreateConstraintRequest model = new CreateConstraintRequest().value("premium");
        Assertions.assertEquals("premium", model.getValue());
    }

}
