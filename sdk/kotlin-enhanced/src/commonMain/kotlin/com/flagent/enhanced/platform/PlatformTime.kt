package com.flagent.enhanced.platform

/**
 * Wall-clock time in milliseconds (epoch). Expect/actual for KMP.
 */
expect fun currentTimeMs(): Long

/**
 * Log warning/error from common code. Expect/actual for KMP.
 */
expect fun logWarn(message: String)
