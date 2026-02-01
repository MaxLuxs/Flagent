package com.flagent.enhanced.platform

actual fun currentTimeMs(): Long = System.currentTimeMillis()

actual fun logWarn(message: String) {
    System.err.println(message)
}
