package flagent.frontend.util

import org.jetbrains.compose.web.css.*

// CSS extensions for compatibility with older Compose for Web API

/**
 * Extension to set border collapse property
 */
fun StyleScope.borderCollapse(value: String) {
    property("border-collapse", value)
}

/**
 * Extension to set border bottom
 */
fun StyleScope.borderBottom(width: CSSNumeric, style: String, color: CSSColorValue) {
    property("border-bottom", "$width $style ${color.toString()}")
}

/**
 * Extension to set border bottom with LineStyle
 */
fun StyleScope.borderBottom(width: CSSNumeric, style: LineStyle, color: CSSColorValue) {
    property("border-bottom", "$width ${style.toString().lowercase()} ${color.toString()}")
}

/**
 * Extension to set border left
 */
fun StyleScope.borderLeft(width: CSSNumeric, style: String, color: CSSColorValue) {
    property("border-left", "$width $style ${color.toString()}")
}

/**
 * Extension to set border left with LineStyle
 */
fun StyleScope.borderLeft(width: CSSNumeric, style: LineStyle, color: CSSColorValue) {
    property("border-left", "$width ${style.toString().lowercase()} ${color.toString()}")
}

/**
 * Extension to set text transform
 */
fun StyleScope.textTransform(value: String) {
    property("text-transform", value)
}

/**
 * Extension to set user select
 */
fun StyleScope.userSelect(value: String) {
    property("user-select", value)
}

/**
 * Extension to set flex-shrink
 */
fun StyleScope.flexShrink(value: Number) {
    property("flex-shrink", value.toString())
}

/**
 * Helper to format Double with decimals
 */
fun Double.format(decimals: Int): String {
    return this.asDynamic().toFixed(decimals) as String
}

/**
 * Helper to format Int as Double with decimals
 */
fun Int.format(decimals: Int): String {
    return this.toDouble().format(decimals)
}

/**
 * Helper to get current time in milliseconds (JS equivalent of System.currentTimeMillis())
 */
fun currentTimeMillis(): Long {
    return (js("Date.now()") as Double).toLong()
}

/**
 * Helper to format date (simple ISO format)
 */
fun Long.formatDate(): String {
    return js("new Date(this).toISOString()") as String
}

/**
 * Generate random ID (UUID-like)
 */
fun randomId(): String {
    return js("crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2) + Date.now().toString(36)") as String
}
