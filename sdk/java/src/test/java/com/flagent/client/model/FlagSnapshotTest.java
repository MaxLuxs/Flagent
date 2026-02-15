package com.flagent.client.model;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for FlagSnapshot
 */
class FlagSnapshotTest {

    @Test
    void testFlagSnapshot() {
        Flag flag = new Flag().id(1L).key("f1").description("d").enabled(true).dataRecordsEnabled(false);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        FlagSnapshot model = new FlagSnapshot()
            .id(1L)
            .updatedBy("admin")
            .flag(flag)
            .updatedAt(updatedAt);
        Assertions.assertNotNull(model);
        Assertions.assertEquals(1L, model.getId());
        Assertions.assertEquals("admin", model.getUpdatedBy());
        Assertions.assertEquals(flag, model.getFlag());
        Assertions.assertEquals(updatedAt, model.getUpdatedAt());
    }

    @Test
    void idTest() {
        FlagSnapshot model = new FlagSnapshot().id(5L);
        Assertions.assertEquals(5L, model.getId());
    }

    @Test
    void updatedByTest() {
        FlagSnapshot model = new FlagSnapshot().updatedBy("user");
        Assertions.assertEquals("user", model.getUpdatedBy());
    }

    @Test
    void flagTest() {
        Flag flag = new Flag().id(10L).key("k").description("d").enabled(true).dataRecordsEnabled(false);
        FlagSnapshot model = new FlagSnapshot().flag(flag);
        Assertions.assertEquals(10L, model.getFlag().getId());
        Assertions.assertEquals("k", model.getFlag().getKey());
    }

    @Test
    void updatedAtTest() {
        OffsetDateTime ts = OffsetDateTime.now();
        FlagSnapshot model = new FlagSnapshot().updatedAt(ts);
        Assertions.assertEquals(ts, model.getUpdatedAt());
    }

}
