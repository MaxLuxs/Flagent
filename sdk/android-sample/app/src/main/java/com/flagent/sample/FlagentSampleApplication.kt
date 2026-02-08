package com.flagent.sample

import android.app.Application
import com.flagent.enhanced.crash.FlagentCrashReporter
import com.flagent.koin.flagentManagerProviderModule
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class that installs Flagent crash reporter and Koin DI.
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

        startKoin {
            androidContext(this@FlagentSampleApplication)
            modules(flagentManagerProviderModule(httpClientEngine = Android.create()))
        }
    }
}
