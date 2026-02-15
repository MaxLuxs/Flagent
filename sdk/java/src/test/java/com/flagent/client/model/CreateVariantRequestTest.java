package com.flagent.client.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for CreateVariantRequest
 */
class CreateVariantRequestTest {

    @Test
    void testCreateVariantRequest() {
        CreateVariantRequest model = new CreateVariantRequest()
            .key("control")
            .attachment(new HashMap<>(Map.of("value", "on")));
        Assertions.assertNotNull(model);
        Assertions.assertEquals("control", model.getKey());
        Assertions.assertNotNull(model.getAttachment());
        Assertions.assertEquals("on", model.getAttachment().get("value"));
    }

    @Test
    void keyTest() {
        CreateVariantRequest model = new CreateVariantRequest().key("variant_a");
        Assertions.assertEquals("variant_a", model.getKey());
    }

    @Test
    void attachmentTest() {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("config", "value");
        CreateVariantRequest model = new CreateVariantRequest().attachment(attachment);
        Assertions.assertEquals("value", model.getAttachment().get("config"));
    }

}
