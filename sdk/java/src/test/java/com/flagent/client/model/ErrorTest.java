package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Error
 */
class ErrorTest {

    @Test
    void testError() {
        Error model = new Error().message("Something went wrong");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("Something went wrong", model.getMessage());
    }

    @Test
    void messageTest() {
        Error model = new Error();
        model.setMessage("Validation failed");
        Assertions.assertEquals("Validation failed", model.getMessage());
    }

}
