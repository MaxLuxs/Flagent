package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for SetFlagEnabledRequest
 */
class SetFlagEnabledRequestTest {

    @Test
    void testSetFlagEnabledRequest() {
        SetFlagEnabledRequest model = new SetFlagEnabledRequest().enabled(true);
        Assertions.assertNotNull(model);
        Assertions.assertTrue(model.getEnabled());
    }

    @Test
    void enabledTest() {
        SetFlagEnabledRequest model = new SetFlagEnabledRequest();
        model.setEnabled(false);
        Assertions.assertFalse(model.getEnabled());
    }

}
