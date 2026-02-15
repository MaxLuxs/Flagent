package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for CreateFlagRequest
 */
class CreateFlagRequestTest {

    @Test
    void testCreateFlagRequest() {
        CreateFlagRequest model = new CreateFlagRequest()
            .description("New flag")
            .key("new_flag")
            .template("boolean");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("New flag", model.getDescription());
        Assertions.assertEquals("new_flag", model.getKey());
        Assertions.assertEquals("boolean", model.getTemplate());
    }

    @Test
    void descriptionTest() {
        CreateFlagRequest model = new CreateFlagRequest().description("Desc");
        Assertions.assertEquals("Desc", model.getDescription());
    }

    @Test
    void keyTest() {
        CreateFlagRequest model = new CreateFlagRequest().key("my_key");
        Assertions.assertEquals("my_key", model.getKey());
    }

    @Test
    void templateTest() {
        CreateFlagRequest model = new CreateFlagRequest().template("json");
        Assertions.assertEquals("json", model.getTemplate());
    }

}
