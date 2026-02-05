package flagent.frontend.config

import kotlinx.browser.window

/**
 * Edition type for Flagent
 */
enum class Edition {
    OPEN_SOURCE,  // Free, self-hosted
    ENTERPRISE    // Paid, with advanced features
}

/**
 * Deployment mode: where the app runs
 */
enum class DeploymentMode {
    SELF_HOSTED,  // Runs on customer's server
    SAAS          // Cloud-hosted product
}

/**
 * Plan / tier: open source, enterprise self-hosted, or SaaS tier
 */
enum class Plan {
    OPEN_SOURCE,      // Self-hosted, free
    ENTERPRISE,       // Self-hosted, paid (multi-tenancy, SSO, Billing, etc.)
    SAAS_ENTERPRISE,  // SaaS, full features
    SAAS_LOWPRICE     // SaaS, reduced features or limits
}

/**
 * Application configuration from environment variables
 */
object AppConfig {
    /**
     * Base URL for API requests
     * Can be set via window.ENV_API_BASE_URL or defaults to current origin
     */
    val apiBaseUrl: String by lazy {
        js("window.ENV_API_BASE_URL") as? String
            ?: window.location.origin
    }
    
    /**
     * Enable debug mode for verbose logging
     */
    val debugMode: Boolean by lazy {
        (js("window.ENV_DEBUG_MODE") as? String)?.toBoolean() ?: false
    }
    
    /**
     * API timeout in milliseconds
     */
    val apiTimeout: Long by lazy {
        (js("window.ENV_API_TIMEOUT") as? String)?.toLongOrNull() ?: 30000L
    }
    
    /**
     * Enable error tracking (Sentry, etc.)
     */
    val errorTrackingEnabled: Boolean by lazy {
        (js("window.ENV_ERROR_TRACKING") as? String)?.toBoolean() ?: false
    }
    
    /**
     * Default language
     */
    val defaultLanguage: String by lazy {
        js("window.ENV_DEFAULT_LANGUAGE") as? String ?: "en"
    }
    
    /**
     * Edition Configuration
     * Set via ENV_EDITION: "open_source" (default) or "enterprise"
     */
    val edition: Edition by lazy {
        when ((js("window.ENV_EDITION") as? String)?.lowercase()) {
            "enterprise" -> Edition.ENTERPRISE
            else -> Edition.OPEN_SOURCE
        }
    }

    /**
     * Deployment mode: ENV_DEPLOYMENT_MODE = "self_hosted" (default) or "saas"
     */
    val deploymentMode: DeploymentMode by lazy {
        when ((js("window.ENV_DEPLOYMENT_MODE") as? String)?.lowercase()) {
            "saas" -> DeploymentMode.SAAS
            else -> DeploymentMode.SELF_HOSTED
        }
    }

    /**
     * Plan / tier: derived from edition + deployment, or ENV_PLAN override.
     * ENV_PLAN: "open_source" | "enterprise" | "saas_enterprise" | "saas_lowprice"
     */
    val plan: Plan by lazy {
        (js("window.ENV_PLAN") as? String)?.lowercase()?.let { env ->
            when (env) {
                "enterprise" -> Plan.ENTERPRISE
                "saas_enterprise" -> Plan.SAAS_ENTERPRISE
                "saas_lowprice" -> Plan.SAAS_LOWPRICE
                else -> Plan.OPEN_SOURCE
            }
        } ?: when {
            deploymentMode == DeploymentMode.SAAS && edition == Edition.ENTERPRISE -> Plan.SAAS_ENTERPRISE
            deploymentMode == DeploymentMode.SAAS && edition == Edition.OPEN_SOURCE -> Plan.SAAS_LOWPRICE
            deploymentMode == DeploymentMode.SELF_HOSTED && edition == Edition.ENTERPRISE -> Plan.ENTERPRISE
            else -> Plan.OPEN_SOURCE
        }
    }

    val isSaaS: Boolean get() = deploymentMode == DeploymentMode.SAAS
    val isSelfHosted: Boolean get() = deploymentMode == DeploymentMode.SELF_HOSTED
    val requiresAuth: Boolean get() = isSaaS || ((js("window.ENV_FEATURE_AUTH") as? String)?.toBoolean() ?: false)

    /**
     * Show full marketing landing at / (Product, Pricing, Blog, Footer).
     * true for SaaS; for self-hosted use ENV_SHOW_LANDING (default false for open-source).
     */
    val showMarketingLanding: Boolean by lazy {
        when (deploymentMode) {
            DeploymentMode.SAAS -> true
            DeploymentMode.SELF_HOSTED -> (js("window.ENV_SHOW_LANDING") as? String)?.toBoolean() ?: false
        }
    }

    /** Documentation URL (GitHub Pages). */
    val docsUrl: String get() = "https://maxluxs.github.io/Flagent/"
    /** GitHub repository URL. */
    val githubUrl: String get() = "https://github.com/MaxLuxs/Flagent"
    /** Blog URL (GitHub Discussions or external). */
    val blogUrl: String get() = "https://github.com/MaxLuxs/Flagent/discussions"

    val isEnterprise: Boolean get() = edition == Edition.ENTERPRISE
    val isOpenSource: Boolean get() = edition == Edition.OPEN_SOURCE

    /**
     * Human-readable deployment + plan label for Settings UI
     */
    fun getDeploymentPlanDisplayLabel(
        selfHostedOpenSource: String,
        selfHostedEnterprise: String,
        saasEnterprise: String,
        saasLowPrice: String
    ): String = when (plan) {
        Plan.OPEN_SOURCE -> selfHostedOpenSource
        Plan.ENTERPRISE -> selfHostedEnterprise
        Plan.SAAS_ENTERPRISE -> saasEnterprise
        Plan.SAAS_LOWPRICE -> saasLowPrice
    }
    
    /**
     * Feature flags for frontend features
     */
    object Features {
        // Core Features (available in both editions)
        val enableAuth: Boolean by lazy {
            (js("window.ENV_FEATURE_AUTH") as? String)?.toBoolean() ?: false
        }
        
        val enableRealtime: Boolean by lazy {
            (js("window.ENV_FEATURE_REALTIME") as? String)?.toBoolean() ?: true
        }
        
        // Metrics, Smart Rollout, Anomaly: only when enterprise backend (hide when open-source)
        val enableMetrics: Boolean by lazy {
            (js("window.ENV_FEATURE_METRICS") as? String)?.toBoolean() ?: true
        }
        
        val enableSmartRollout: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_SMART_ROLLOUT") as? String)?.toBoolean() ?: true)
        }
        
        val enableAnomalyDetection: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_ANOMALY_DETECTION") as? String)?.toBoolean() ?: true)
        }
        
        // Enterprise-Only Features (only available in Enterprise edition)
        val enableMultiTenancy: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_MULTI_TENANCY") as? String)?.toBoolean() ?: true)
        }
        
        val enableSso: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_SSO") as? String)?.toBoolean() ?: true)
        }
        
        val enableBilling: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_BILLING") as? String)?.toBoolean() ?: true)
        }
        
        val enableSlack: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_SLACK") as? String)?.toBoolean() ?: true)
        }
        
        val enableAdvancedAnalytics: Boolean by lazy {
            (plan == Plan.ENTERPRISE || plan == Plan.SAAS_ENTERPRISE) &&
                ((js("window.ENV_FEATURE_ADVANCED_ANALYTICS") as? String)?.toBoolean() ?: true)
        }

        val enableAuditLogs: Boolean by lazy {
            (plan == Plan.ENTERPRISE || plan == Plan.SAAS_ENTERPRISE) &&
                ((js("window.ENV_FEATURE_AUDIT_LOGS") as? String)?.toBoolean() ?: true)
        }
        
        val enableRbac: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_RBAC") as? String)?.toBoolean() ?: true)
        }

        /** Crash Analytics dashboard (Enterprise only). */
        val enableCrashAnalytics: Boolean by lazy {
            isEnterprise && ((js("window.ENV_FEATURE_CRASH_ANALYTICS") as? String)?.toBoolean() ?: true)
        }
    }
}
