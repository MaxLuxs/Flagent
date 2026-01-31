package com.flagent.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthType {
    NONE,
    BASIC,
    BEARER
}

data class AppSettings(
    val baseUrl: String = "http://10.0.2.2:18000/api/v1",
    val cacheEnabled: Boolean = true,
    val cacheTtlMs: Long = 60_000L,
    val authType: AuthType = AuthType.NONE,
    val authUsername: String = "",
    val authToken: String = ""
)

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateBaseUrl(url: String) {
        _settings.update { it.copy(baseUrl = url) }
    }

    fun updateCache(enabled: Boolean, ttlMs: Long = 60_000L) {
        _settings.update { it.copy(cacheEnabled = enabled, cacheTtlMs = ttlMs) }
    }

    fun updateAuth(type: AuthType, username: String = "", token: String = "") {
        _settings.update {
            it.copy(
                authType = type,
                authUsername = username,
                authToken = token
            )
        }
    }
}
