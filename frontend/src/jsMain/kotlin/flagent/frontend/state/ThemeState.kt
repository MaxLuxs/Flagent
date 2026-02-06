package flagent.frontend.state

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.browser.document
import kotlinx.browser.localStorage

private const val THEME_STORAGE_KEY = "flagent_theme"

enum class ThemeMode { Dark, Light }

object ThemeState {
    val current = mutableStateOf(ThemeMode.Dark)

    init {
        val stored = localStorage.getItem(THEME_STORAGE_KEY)
        current.value = when (stored?.lowercase()) {
            "light" -> ThemeMode.Light
            "dark" -> ThemeMode.Dark
            else -> ThemeMode.Dark
        }
        applyToDocument(current.value)
    }

    fun toggle() {
        current.value = if (current.value == ThemeMode.Dark) ThemeMode.Light else ThemeMode.Dark
        persistAndApply()
    }

    fun set(mode: ThemeMode) {
        current.value = mode
        persistAndApply()
    }

    private fun persistAndApply() {
        localStorage.setItem(THEME_STORAGE_KEY, current.value.name.lowercase())
        applyToDocument(current.value)
    }

    fun applyToDocument(mode: ThemeMode) {
        document.documentElement?.setAttribute("data-theme", mode.name.lowercase())
    }
}

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.Dark }
