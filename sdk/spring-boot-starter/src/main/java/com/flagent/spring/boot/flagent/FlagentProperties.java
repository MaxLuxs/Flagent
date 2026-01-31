package com.flagent.spring.boot.flagent;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Flagent client (prefix: flagent).
 */
@ConfigurationProperties(prefix = "flagent")
public class FlagentProperties {

    /**
     * Base URL of the Flagent server (e.g. http://localhost:18000).
     * The path /api/v1 is appended if not present.
     */
    private String baseUrl = "http://localhost:18000";

    /**
     * Connect timeout in milliseconds.
     */
    private long connectTimeoutMs = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private long readTimeoutMs = 10000;

    /**
     * Whether the Flagent client is enabled.
     */
    private boolean enabled = true;

    private Cache cache = new Cache();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class Cache {
        private boolean enabled = true;
        private long ttlMs = 60000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTtlMs() {
            return ttlMs;
        }

        public void setTtlMs(long ttlMs) {
            this.ttlMs = ttlMs;
        }
    }
}
