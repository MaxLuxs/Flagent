package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun createDefaultHttpClientEngine(): HttpClientEngine = CIO.create()
