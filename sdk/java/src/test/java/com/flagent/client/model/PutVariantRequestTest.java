package com.flagent.client.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutVariantRequest
 */
class PutVariantRequestTest {

    @Test
    void testPutVariantRequest() {
        PutVariantRequest model = new PutVariantRequest()
            .key("updated_key")
            .attachment(new HashMap<>(Map.of("k", "v")));
        Assertions.assertNotNull(model);
        Assertions.assertEquals("updated_key", model.getKey());
        Assertions.assertEquals("v", model.getAttachment().get("k"));
    }

    @Test
    void keyTest() {
        PutVariantRequest model = new PutVariantRequest().key("key");
        Assertions.assertEquals("key", model.getKey());
    }

    @Test
    void attachmentTest() {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("x", 1);
        PutVariantRequest model = new PutVariantRequest().attachment(attachment);
        Assertions.assertEquals(1, model.getAttachment().get("x"));
    }

}
