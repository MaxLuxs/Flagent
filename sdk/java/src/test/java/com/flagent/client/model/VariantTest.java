package com.flagent.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Variant
 */
class VariantTest {

    @Test
    void testVariant() {
        Variant model = new Variant().id(1L).flagID(10L).key("control");
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals(10L, model.getFlagID());
        Assertions.assertEquals("control", model.getKey());
    }

    @Test
    void idTest() {
        Variant model = new Variant().id(5L);
        Assertions.assertEquals(5L, model.getId());
    }

    @Test
    void flagIDTest() {
        Variant model = new Variant().flagID(20L);
        Assertions.assertEquals(20L, model.getFlagID());
    }

    @Test
    void keyTest() {
        Variant model = new Variant().key("variant_a");
        Assertions.assertEquals("variant_a", model.getKey());
    }

    @Test
    void attachmentTest() {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("config", "value");
        Variant model = new Variant().attachment(attachment);
        Assertions.assertNotNull(model.getAttachment());
        Assertions.assertEquals("value", model.getAttachment().get("config"));
    }

}
