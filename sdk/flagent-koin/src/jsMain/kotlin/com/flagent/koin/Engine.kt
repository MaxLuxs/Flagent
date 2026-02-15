package com.flagent.koin

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun defaultHttpClientEngine(): HttpClientEngine = Js.create()
