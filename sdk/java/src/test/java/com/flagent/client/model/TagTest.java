package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Tag
 */
class TagTest {

    @Test
    void testTag() {
        Tag model = new Tag().id(1L).value("production");
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals("production", model.getValue());
    }

    @Test
    void idTest() {
        Tag model = new Tag().id(99L);
        Assertions.assertEquals(99L, model.getId());
    }

    @Test
    void valueTest() {
        Tag model = new Tag().value("staging");
        Assertions.assertEquals("staging", model.getValue());
    }

}
