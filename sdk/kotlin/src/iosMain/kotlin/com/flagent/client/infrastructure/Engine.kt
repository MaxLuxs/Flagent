package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun createDefaultHttpClientEngine(): HttpClientEngine = Darwin.create()
