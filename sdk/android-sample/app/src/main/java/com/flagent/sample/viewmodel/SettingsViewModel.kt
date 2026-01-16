package com.flagent.sample.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flagent.enhanced.config.FlagentConfig
import com.flagent.sample.BuildConfig
import com.flagent.sample.di.AppModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private object PreferenceKeys {
    val BASE_URL = stringPreferencesKey("base_url")
    val CACHE_ENABLED = booleanPreferencesKey("cache_enabled")
    val CACHE_TTL_MS = longPreferencesKey("cache_ttl_ms")
    val AUTH_TYPE = stringPreferencesKey("auth_type")
    val AUTH_USERNAME = stringPreferencesKey("auth_username")
    val AUTH_PASSWORD = stringPreferencesKey("auth_password")
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
}

enum class AuthType {
    NONE, BASIC, BEARER
}

data class AppSettings(
    val baseUrl: String = BuildConfig.DEFAULT_BASE_URL,
    val cacheEnabled: Boolean = true,
    val cacheTtlMs: Long = 5 * 60 * 1000L,
    val authType: AuthType = AuthType.NONE,
    val authUsername: String = "",
    val authPassword: String = "",
    val authToken: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                AppSettings(
                    baseUrl = preferences[PreferenceKeys.BASE_URL] ?: BuildConfig.DEFAULT_BASE_URL,
                    cacheEnabled = preferences[PreferenceKeys.CACHE_ENABLED] ?: true,
                    cacheTtlMs = preferences[PreferenceKeys.CACHE_TTL_MS] ?: (5 * 60 * 1000L),
                    authType = AuthType.valueOf(
                        preferences[PreferenceKeys.AUTH_TYPE] ?: AuthType.NONE.name
                    ),
                    authUsername = preferences[PreferenceKeys.AUTH_USERNAME] ?: "",
                    authPassword = preferences[PreferenceKeys.AUTH_PASSWORD] ?: "",
                    authToken = preferences[PreferenceKeys.AUTH_TOKEN] ?: ""
                )
            }.collect { loadedSettings ->
                _settings.value = loadedSettings
                applySettings(loadedSettings)
            }
        }
    }
    
    fun updateBaseUrl(url: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.BASE_URL] = url
            }
        }
    }
    
    fun updateCacheEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.CACHE_ENABLED] = enabled
            }
        }
    }
    
    fun updateCacheTtl(ttlMs: Long) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.CACHE_TTL_MS] = ttlMs
            }
        }
    }
    
    fun updateAuthType(authType: AuthType) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.AUTH_TYPE] = authType.name
            }
            // Apply auth immediately
            applySettings(_settings.value.copy(authType = authType))
        }
    }
    
    fun updateAuthCredentials(username: String, password: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.AUTH_USERNAME] = username
                preferences[PreferenceKeys.AUTH_PASSWORD] = password
            }
            // Apply auth immediately
            applySettings(_settings.value.copy(authUsername = username, authPassword = password))
        }
    }
    
    fun updateAuthToken(token: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.AUTH_TOKEN] = token
            }
            // Apply auth immediately
            applySettings(_settings.value.copy(authToken = token))
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            val manager = AppModule.getFlagentManager(
                _settings.value.baseUrl,
                FlagentConfig(
                    enableCache = _settings.value.cacheEnabled,
                    cacheTtlMs = _settings.value.cacheTtlMs
                )
            )
            manager.clearCache()
        }
    }
    
    private fun applySettings(settings: AppSettings) {
        viewModelScope.launch {
            val config = FlagentConfig(
                enableCache = settings.cacheEnabled,
                cacheTtlMs = settings.cacheTtlMs
            )
            
            AppModule.recreate(settings.baseUrl, config)
            
            when (settings.authType) {
                AuthType.BASIC -> {
                    AppModule.configureBasicAuth(settings.authUsername, settings.authPassword)
                }
                AuthType.BEARER -> {
                    AppModule.configureBearerAuth(settings.authToken)
                }
                AuthType.NONE -> {
                    // No auth
                }
            }
        }
    }
}
