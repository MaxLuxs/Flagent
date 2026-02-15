package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.curl.Curl

actual fun createDefaultHttpClientEngine(): HttpClientEngine = Curl.create()
