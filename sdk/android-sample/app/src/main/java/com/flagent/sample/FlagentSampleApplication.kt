package com.flagent.sample

import android.app.Application
import com.flagent.enhanced.crash.FlagentCrashReporter

/**
 * Application class that installs Flagent crash reporter.
 * Captures uncaught exceptions and sends to Flagent backend.
 */
class FlagentSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val baseUrl = BuildConfig.DEFAULT_BASE_URL
            .removeSuffix("/api/v1")
            .removeSuffix("/api")
            .removeSuffix("/")
        FlagentCrashReporter(
            baseUrl = baseUrl,
            apiKey = null,
            platform = "android",
            appVersion = BuildConfig.VERSION_NAME,
            deviceInfo = "Android ${android.os.Build.VERSION.SDK_INT}, ${android.os.Build.MODEL}"
        ).install()
    }
}
