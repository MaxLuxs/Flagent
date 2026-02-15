package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for PutFlagRequest
 */
class PutFlagRequestTest {

    @Test
    void testPutFlagRequest() {
        PutFlagRequest model = new PutFlagRequest()
            .description("Updated")
            .key("key")
            .dataRecordsEnabled(true)
            .entityType("user")
            .notes("Notes");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("Updated", model.getDescription());
        Assertions.assertEquals("key", model.getKey());
        Assertions.assertTrue(model.getDataRecordsEnabled());
        Assertions.assertEquals("user", model.getEntityType());
        Assertions.assertEquals("Notes", model.getNotes());
    }

    @Test
    void descriptionTest() {
        PutFlagRequest model = new PutFlagRequest().description("Desc");
        Assertions.assertEquals("Desc", model.getDescription());
    }

    @Test
    void keyTest() {
        PutFlagRequest model = new PutFlagRequest().key("my_key");
        Assertions.assertEquals("my_key", model.getKey());
    }

    @Test
    void dataRecordsEnabledTest() {
        PutFlagRequest model = new PutFlagRequest().dataRecordsEnabled(true);
        Assertions.assertTrue(model.getDataRecordsEnabled());
    }

    @Test
    void entityTypeTest() {
        PutFlagRequest model = new PutFlagRequest().entityType("device");
        Assertions.assertEquals("device", model.getEntityType());
    }

    @Test
    void notesTest() {
        PutFlagRequest model = new PutFlagRequest().notes("Note");
        Assertions.assertEquals("Note", model.getNotes());
    }

}
