package com.flagent.enhanced.platform

import kotlinx.datetime.Clock

actual fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()

actual fun logWarn(message: String) {
    println(message)
}
