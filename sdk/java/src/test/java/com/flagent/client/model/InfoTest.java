package com.flagent.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Model tests for Info
 */
class InfoTest {

    @Test
    void testInfo() {
        Info model = new Info().version("1.0.0").buildTime("2024-01-01T00:00:00Z").gitCommit("abc123");
        Assertions.assertNotNull(model);
        Assertions.assertEquals("1.0.0", model.getVersion());
        Assertions.assertEquals("2024-01-01T00:00:00Z", model.getBuildTime());
        Assertions.assertEquals("abc123", model.getGitCommit());
    }

    @Test
    void versionTest() {
        Info model = new Info().version("2.0.0");
        Assertions.assertEquals("2.0.0", model.getVersion());
    }

    @Test
    void buildTimeTest() {
        Info model = new Info().buildTime("2024-06-01T12:00:00Z");
        Assertions.assertEquals("2024-06-01T12:00:00Z", model.getBuildTime());
    }

    @Test
    void gitCommitTest() {
        Info model = new Info().gitCommit("def456");
        Assertions.assertEquals("def456", model.getGitCommit());
    }

}
