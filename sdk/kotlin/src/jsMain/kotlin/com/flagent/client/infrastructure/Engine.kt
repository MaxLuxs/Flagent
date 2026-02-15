package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createDefaultHttpClientEngine(): HttpClientEngine = Js.create()
