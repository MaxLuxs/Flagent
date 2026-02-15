package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutConstraintRequest
 */
class PutConstraintRequestTest {

    @Test
    void testPutConstraintRequest() {
        PutConstraintRequest model = new PutConstraintRequest()
            .property("region")
            .operator("EQ")
            .value("EU");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("region", model.getProperty());
        Assertions.assertEquals("EQ", model.getOperator());
        Assertions.assertEquals("EU", model.getValue());
    }

    @Test
    void propertyTest() {
        PutConstraintRequest model = new PutConstraintRequest().property("tier");
        Assertions.assertEquals("tier", model.getProperty());
    }

    @Test
    void operatorTest() {
        PutConstraintRequest model = new PutConstraintRequest().operator("NEQ");
        Assertions.assertEquals("NEQ", model.getOperator());
    }

    @Test
    void valueTest() {
        PutConstraintRequest model = new PutConstraintRequest().value("free");
        Assertions.assertEquals("free", model.getValue());
    }

}
