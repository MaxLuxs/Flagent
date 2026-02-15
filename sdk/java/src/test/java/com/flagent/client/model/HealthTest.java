package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Health
 */
class HealthTest {

    @Test
    void testHealth() {
        Health model = new Health().status("ok");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("ok", model.getStatus());
    }

    @Test
    void statusTest() {
        Health model = new Health();
        model.setStatus("degraded");
        Assertions.assertEquals("degraded", model.getStatus());
    }

}
