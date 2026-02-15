package com.flagent.koin

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun defaultHttpClientEngine(): HttpClientEngine = Android.create()
