package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun createDefaultHttpClientEngine(): HttpClientEngine = Android.create()
