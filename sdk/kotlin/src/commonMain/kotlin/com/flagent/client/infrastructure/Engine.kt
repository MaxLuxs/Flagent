package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine

/**
 * Platform-specific default HTTP client engine.
 * Used when no engine is passed to ApiClient (e.g. CIO on JVM, Darwin on iOS).
 */
expect fun createDefaultHttpClientEngine(): HttpClientEngine

