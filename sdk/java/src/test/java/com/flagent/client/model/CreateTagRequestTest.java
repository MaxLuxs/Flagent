package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for CreateTagRequest
 */
class CreateTagRequestTest {

    @Test
    void testCreateTagRequest() {
        CreateTagRequest model = new CreateTagRequest().value("production");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("production", model.getValue());
    }

    @Test
    void valueTest() {
        CreateTagRequest model = new CreateTagRequest().value("staging");
        Assertions.assertEquals("staging", model.getValue());
    }

}
