package com.flagent.annotations

/**
 * Marks a property or field as a Flagent flag key.
 * Use with build-time verification (Gradle plugin `verify-flags`) so that keys are discovered
 * from code and checked against Flagent API or flags file.
 *
 * Example:
 * ```
 * object AppFlags {
 *     @FlagKey("new_checkout_flow")
 *     const val NEW_CHECKOUT_FLOW = "new_checkout_flow"
 * }
 * client.isEnabled(AppFlags.NEW_CHECKOUT_FLOW, entityId, context)
 * ```
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class FlagKey(val value: String)
