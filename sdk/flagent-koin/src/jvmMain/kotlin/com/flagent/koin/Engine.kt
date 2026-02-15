package com.flagent.koin

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun defaultHttpClientEngine(): HttpClientEngine = CIO.create()
