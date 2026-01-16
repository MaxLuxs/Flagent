package io.ktor.flagent

import io.ktor.server.application.*

/**
 * FlagentPluginConfig - configuration for FlagentPlugin
 * This file provides extension functions and additional configuration options
 */

/**
 * Extension function to get FlagentClient from plugin
 */
fun Application.getFlagentClient(): FlagentClient? {
    return attributes.getOrNull(FlagentPluginAttributes.client)
}

/**
 * Extension function to get FlagentCache from plugin
 */
fun Application.getFlagentCache(): FlagentCache? {
    return attributes.getOrNull(FlagentPluginAttributes.cache)
}

/**
 * Extension function to get FlagentMetrics from plugin
 */
fun Application.getFlagentMetrics(): FlagentMetrics? {
    return attributes.getOrNull(FlagentPluginAttributes.metrics)
}
