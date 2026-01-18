package com.flagent.sample.di

import android.content.Context
import com.flagent.client.apis.EvaluationApi
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.enhanced.manager.FlagentManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object AppModule {
    private var httpClient: HttpClient? = null
    private var evaluationApi: EvaluationApi? = null
    private var flagentManager: FlagentManager? = null
    
    private var currentBaseUrl: String = ""
    private var currentConfig: FlagentConfig = FlagentConfig()
    
    fun getHttpClient(baseUrl: String): HttpClient {
        if (httpClient == null || currentBaseUrl != baseUrl) {
            httpClient = HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = false
                    })
                }
            }
            currentBaseUrl = baseUrl
        }
        return httpClient!!
    }
    
    fun getEvaluationApi(baseUrl: String): EvaluationApi {
        if (evaluationApi == null || currentBaseUrl != baseUrl) {
            val client = getHttpClient(baseUrl)
            evaluationApi = EvaluationApi(
                baseUrl = baseUrl,
                httpClientEngine = client.engine
            )
        }
        return evaluationApi!!
    }
    
    fun getFlagentManager(
        baseUrl: String,
        config: FlagentConfig = FlagentConfig()
    ): FlagentManager {
        if (flagentManager == null || 
            currentBaseUrl != baseUrl || 
            currentConfig != config) {
            val api = getEvaluationApi(baseUrl)
            flagentManager = FlagentManager(api, config)
            currentConfig = config
        }
        return flagentManager!!
    }
    
    fun configureBasicAuth(username: String, password: String) {
        evaluationApi?.let { api ->
            api.setUsername(username)
            api.setPassword(password)
        }
    }
    
    fun configureBearerAuth(token: String) {
        evaluationApi?.let { api ->
            api.setBearerToken(token)
        }
    }
    
    fun clearAuth() {
        httpClient = null
        evaluationApi = null
        flagentManager = null
    }
    
    fun recreate(baseUrl: String, config: FlagentConfig = FlagentConfig()) {
        clearAuth()
        getFlagentManager(baseUrl, config)
    }
}
