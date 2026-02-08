package com.flagent.sample.di

import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import com.flagent.koin.FlagentManagerProvider
import org.koin.core.context.GlobalContext

/**
 * Legacy AppModule that delegates to Koin's FlagentManagerProvider.
 * Keeps backward compatibility with ViewModels and screens.
 */
object AppModule {
    private val provider: FlagentManagerProvider
        get() = GlobalContext.get().get<FlagentManagerProvider>()

    fun getEvaluationApi(baseUrl: String): EvaluationApi = provider.getEvaluationApi(baseUrl)

    fun getFlagentManager(
        baseUrl: String,
        config: FlagentConfig = FlagentConfig()
    ): FlagentManager = provider.getManager(baseUrl, config)
}
