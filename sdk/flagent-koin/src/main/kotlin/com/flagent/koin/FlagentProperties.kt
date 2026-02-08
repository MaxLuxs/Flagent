package com.flagent.koin

/**
 * Configuration properties for Flagent Koin module.
 * Analogous to FlagentProperties in Spring Boot starter.
 */
data class FlagentProperties(
    val baseUrl: String = "http://localhost:18000",
    val connectTimeoutMs: Int = 5000,
    val readTimeoutMs: Int = 10000,
    val cache: CacheProperties = CacheProperties()
)

data class CacheProperties(
    val enabled: Boolean = true,
    val ttlMs: Long = 60000
)
