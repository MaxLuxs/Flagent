package flagent.frontend.state

import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.document
import kotlinx.browser.localStorage

private const val LOCALE_STORAGE_KEY = "flagent_locale"

/**
 * Language state with persistence in browser localStorage.
 * Used by LocalizedStrings for runtime language switching.
 */
object LanguageState {
    val current = mutableStateOf("en")

    init {
        val stored = localStorage.getItem(LOCALE_STORAGE_KEY)
        val normalized = normalize(stored)
        current.value = normalized
        applyToDocument(normalized)
    }

    fun set(locale: String) {
        val normalized = normalize(locale)
        current.value = normalized
        localStorage.setItem(LOCALE_STORAGE_KEY, normalized)
        applyToDocument(normalized)
    }

    private fun normalize(locale: String?): String =
        when (locale?.lowercase()) {
            "ru" -> "ru"
            "en" -> "en"
            else -> "en"
        }

    private fun applyToDocument(locale: String) {
        document.documentElement?.setAttribute("lang", locale)
    }
}

