package flagent.frontend.theme

import flagent.frontend.state.ThemeMode
import org.jetbrains.compose.web.css.*

/**
 * Flagent Design System Theme.
 * Values are driven by design-system/tokens/tokens.json (see flagent-tokens.css).
 * All colors should be accessed through this theme object.
 */
object FlagentTheme {
    private fun v(name: String) = "var(--flagent-$name)"

    // Semantic (theme-independent) — from tokens
    val Primary = Color(v("color-primary"))
    val PrimaryDark = Color(v("color-primaryDark"))
    val PrimaryLight = Color(v("color-primaryLight"))
    val Secondary = Color(v("color-secondary"))
    val SecondaryDark = Color(v("color-secondaryDark"))
    val SecondaryLight = Color(v("color-secondaryLight"))
    val Accent = Color(v("color-accent"))
    val Background = Color(v("color-light-background"))
    val BackgroundAlt = Color(v("color-light-backgroundAlt"))
    val BackgroundDark = Color(v("color-light-backgroundDark"))
    val Text = Color(v("color-light-text"))
    val TextLight = Color(v("color-light-textLight"))
    val TextLighter = Color(v("color-light-textLighter"))
    val Border = Color(v("color-light-border"))
    val CodeBackground = Color(v("color-codeBackground"))
    val CodeText = Color(v("color-codeText"))
    val Success = Color(v("color-success"))
    val Error = Color(v("color-error"))
    val Warning = Color(v("color-warning"))
    val Info = Color(v("color-info"))
    val Neutral = Color(v("color-neutral"))
    val NeutralLight = Color(v("color-neutralLight"))
    val NeutralLighter = Color(v("color-neutralLighter"))
    val Shadow = v("shadow-default")
    val ShadowHover = v("shadow-hover")
    /** Full box-shadow presets (use in property("box-shadow", ...)) */
    val ShadowCard = v("shadow-card")
    val ShadowModal = v("shadow-modal")
    val Overlay = Color(v("color-overlay"))
    val PrimaryGlow = Color(v("color-primaryGlow"))
    /** CSS gradient strings (use in style background/backgroundImage) */
    val GradientPrimary = v("gradient-primary")
    val GradientSecondary = v("gradient-secondary")
    val GradientHero = v("gradient-hero")

    // Dark workspace (raw var names for when mode is Dark)
    val WorkspaceBackground = Color(v("color-dark-background"))
    val WorkspaceSidebarBg = Color(v("color-dark-sidebarBg"))
    val WorkspaceContentBg = Color(v("color-dark-contentBg"))
    val WorkspaceText = Color(v("color-dark-text"))
    val WorkspaceTextLight = Color(v("color-dark-textLight"))
    val WorkspaceBorder = Color(v("color-dark-border"))
    val WorkspaceCardBg = Color(v("color-dark-cardBg"))
    val WorkspaceCardBorder = Color(v("color-dark-cardBorder"))
    val WorkspaceInputBg = Color(v("color-dark-inputBg"))
    val WorkspaceInputBorder = Color(v("color-dark-inputBorder"))
    val WorkspaceTextMuted = Color(v("color-dark-textMuted"))

    /** Theme-aware colors based on current ThemeMode */
    fun cardBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-cardBg"))
        ThemeMode.Light -> Color(v("color-light-cardBg"))
    }
    fun cardBorder(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-cardBorder"))
        ThemeMode.Light -> Color(v("color-light-cardBorder"))
    }
    fun inputBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-inputBg"))
        ThemeMode.Light -> Color(v("color-light-inputBg"))
    }
    fun inputBorder(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-inputBorder"))
        ThemeMode.Light -> Color(v("color-light-inputBorder"))
    }
    fun text(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-text"))
        ThemeMode.Light -> Color(v("color-light-text"))
    }
    fun textLight(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-textLight"))
        ThemeMode.Light -> Color(v("color-light-textLight"))
    }
    fun contentBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-contentBg"))
        ThemeMode.Light -> Color(v("color-light-backgroundAlt"))
    }
    fun sidebarBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-sidebarBg"))
        ThemeMode.Light -> Color(v("color-light-backgroundDark"))
    }
    fun errorBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-errorBg"))
        ThemeMode.Light -> Color(v("color-light-errorBg"))
    }
    fun errorText(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-errorText"))
        ThemeMode.Light -> Color(v("color-light-errorText"))
    }
    fun badgeBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-badgeBg"))
        ThemeMode.Light -> Color(v("color-light-badgeBg"))
    }
    fun badgeText(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> textLight(mode)
        ThemeMode.Light -> Color(v("color-light-textLight"))
    }
    fun successBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-successBg"))
        ThemeMode.Light -> Color(v("color-light-successBg"))
    }
    fun warningBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-warningBg"))
        ThemeMode.Light -> Color(v("color-light-warningBg"))
    }
    fun infoBg(mode: ThemeMode) = when (mode) {
        ThemeMode.Dark -> Color(v("color-dark-infoBg"))
        ThemeMode.Light -> Color(v("color-light-infoBg"))
    }

    /** CSS variable string for use in property("border", "1px solid ${FlagentTheme.cssVar(\"color-dark-cardBorder\")}") etc. */
    fun cssVar(suffix: String) = "var(--flagent-$suffix)"
}
