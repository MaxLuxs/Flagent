package flagent.frontend.theme

import org.jetbrains.compose.web.css.*

/**
 * Flagent Design System Theme
 * 
 * Unified color palette based on Teal/Blue scheme
 * All colors should be accessed through this theme object
 */
object FlagentTheme {
    // Primary colors (Sky Blue)
    val Primary = Color("#0EA5E9")
    val PrimaryDark = Color("#0284C7")
    val PrimaryLight = Color("#38BDF8")
    
    // Secondary colors (Teal)
    val Secondary = Color("#14B8A6")
    val SecondaryDark = Color("#0D9488")
    val SecondaryLight = Color("#5EEAD4")
    
    // Accent colors
    val Accent = Color("#F59E0B")
    
    // Background colors
    val Background = Color("#FFFFFF")
    val BackgroundAlt = Color("#F8FAFC")
    val BackgroundDark = Color("#F1F5F9")
    
    // Text colors
    val Text = Color("#0F172A")
    val TextLight = Color("#64748B")
    val TextLighter = Color("#94A3B8")
    
    // Border colors
    val Border = Color("#E2E8F0")
    
    // Code colors
    val CodeBackground = Color("#1E293B")
    val CodeText = Color("#CBD5E1")
    
    // Status colors
    val Success = Color("#10B981")
    val Error = Color("#EF4444")
    val Warning = Color("#F59E0B")
    val Info = Color("#3B82F6")
    
    // Neutral colors (for buttons, disabled states, etc.)
    val Neutral = Color("#6B7280")
    val NeutralLight = Color("#9CA3AF")
    val NeutralLighter = Color("#D1D5DB")
    
    // Shadow
    val Shadow = "rgba(0, 0, 0, 0.08)"
    val ShadowHover = "rgba(0, 0, 0, 0.15)"

    // Workspace dark theme (aligned with landing/login design, no animations)
    val WorkspaceBackground = Color("#0f172a")
    val WorkspaceSidebarBg = Color("#1e293b")
    val WorkspaceContentBg = Color("#0f172a")
    val WorkspaceText = Color("#FFFFFF")
    val WorkspaceTextLight = Color("rgba(255,255,255,0.7)")
    val WorkspaceBorder = Color("rgba(255,255,255,0.08)")
    val WorkspaceCardBg = Color("rgba(255,255,255,0.04)")
    val WorkspaceCardBorder = Color("rgba(255,255,255,0.08)")
    val WorkspaceInputBg = Color("rgba(255,255,255,0.06)")
    val WorkspaceInputBorder = Color("rgba(255,255,255,0.12)")
}
