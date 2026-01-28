package flagent.repository.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.json.json

/**
 * Flags table
 */
object Flags : IntIdTable("flags") {
    val key = varchar("key", 64).uniqueIndex("idx_flag_key")
    val description = text("description")
    val createdBy = varchar("created_by", 255).nullable()
    val updatedBy = varchar("updated_by", 255).nullable()
    val enabled = bool("enabled").default(false)
    val snapshotId = integer("snapshot_id").default(0)
    val notes = text("notes").nullable()
    val dataRecordsEnabled = bool("data_records_enabled").default(false)
    val entityType = varchar("entity_type", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Segments table
 */
object Segments : IntIdTable("segments") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_segment_flagid")
    val description = text("description").nullable()
    val rank = integer("rank").default(999)
    val rolloutPercent = integer("rollout_percent").default(0)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Variants table
 */
object Variants : IntIdTable("variants") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_variant_flagid")
    val key = varchar("key", 255).nullable()
    val attachment = text("attachment").nullable() // JSON stored as text
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Constraints table
 */
object Constraints : IntIdTable("constraints") {
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE).index("idx_constraint_segmentid")
    val property = varchar("property", 255)
    val operator = varchar("operator", 50)
    val value = text("value")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Distributions table
 */
object Distributions : IntIdTable("distributions") {
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE).index("idx_distribution_segmentid")
    val variantId = integer("variant_id").references(Variants.id, onDelete = ReferenceOption.CASCADE).index("idx_distribution_variantid")
    val variantKey = varchar("variant_key", 255).nullable()
    val percent = integer("percent").default(0) // 0-100
    val bitmap = text("bitmap").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Tags table
 */
object Tags : IntIdTable("tags") {
    val value = varchar("value", 64).uniqueIndex("idx_tag_value")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * FlagsTags junction table for many-to-many relationship
 * Maps to flags_tags table from GORM many2many
 */
object FlagsTags : IntIdTable("flags_tags") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
    val tagId = integer("tag_id").references(Tags.id, onDelete = ReferenceOption.CASCADE)
    
    init {
        uniqueIndex("flags_tags_unique", flagId, tagId)
    }
}

/**
 * FlagSnapshots table
 */
object FlagSnapshots : IntIdTable("flag_snapshots") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE).index("idx_flagsnapshot_flagid")
    val updatedBy = varchar("updated_by", 255).nullable()
    val flag = text("flag") // JSON stored as text
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * FlagEntityTypes table
 */
object FlagEntityTypes : IntIdTable("flag_entity_types") {
    val key = varchar("key", 64).uniqueIndex("flag_entity_type_key")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * Users table
 */
object Users : IntIdTable("users") {
    val email = text("email").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val deletedAt = datetime("deleted_at").nullable()
}

// ============================================================================
// MULTI-TENANCY TABLES (in public schema)
// ============================================================================

/**
 * Tenants table - manages SaaS tenants.
 *
 * Schema-per-tenant approach:
 * - Each tenant has a dedicated schema (tenant_<id>)
 * - Strong isolation between tenants
 * - All Flagent tables duplicated per tenant
 */
object Tenants : LongIdTable("tenants") {
    val key = varchar("key", 255).uniqueIndex("idx_tenant_key")
    val name = varchar("name", 255)
    val plan = varchar("plan", 50) // STARTER, GROWTH, SCALE, ENTERPRISE
    val status = varchar("status", 50) // ACTIVE, SUSPENDED, CANCELLED, DELETED
    val tenantSchemaName = varchar("schema_name", 255).uniqueIndex("idx_tenant_schema")
    val stripeCustomerId = varchar("stripe_customer_id", 255).nullable().uniqueIndex("idx_tenant_stripe_customer")
    val stripeSubscriptionId = varchar("stripe_subscription_id", 255).nullable()
    val billingEmail = varchar("billing_email", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val deletedAt = datetime("deleted_at").nullable()
}

/**
 * TenantUsers table - users within tenants.
 *
 * Cross-tenant user management:
 * - Users can belong to multiple tenants
 * - Different roles per tenant
 */
object TenantUsers : LongIdTable("tenant_users") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_tenant_user_tenant")
    val email = varchar("email", 255)
    val role = varchar("role", 50) // OWNER, ADMIN, MEMBER, VIEWER
    val createdAt = datetime("created_at")
    
    init {
        uniqueIndex("tenant_users_unique", tenantId, email)
    }
}

/**
 * TenantApiKeys table - API keys for tenant access.
 *
 * Security:
 * - Keys are hashed (SHA-256)
 * - Scopes for fine-grained access control
 * - Expiration support
 * - Usage tracking
 */
object TenantApiKeys : LongIdTable("tenant_api_keys") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_tenant_apikey_tenant")
    val keyHash = varchar("key_hash", 255).uniqueIndex("idx_tenant_apikey_hash")
    val name = varchar("name", 255)
    val scopes = text("scopes") // JSON array of scopes
    val expiresAt = datetime("expires_at").nullable()
    val createdAt = datetime("created_at")
    val lastUsedAt = datetime("last_used_at").nullable()
}

/**
 * TenantUsage table - tracks usage for billing.
 *
 * Metered usage tracking:
 * - Daily aggregation
 * - Evaluations count (primary billing metric)
 * - Flags count (for limits enforcement)
 * - API calls count (for rate limiting)
 */
object TenantUsage : LongIdTable("tenant_usage") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_tenant_usage_tenant")
    val periodStart = date("period_start")
    val periodEnd = date("period_end")
    val evaluationsCount = long("evaluations_count").default(0)
    val flagsCount = integer("flags_count").default(0)
    val apiCallsCount = long("api_calls_count").default(0)
    val createdAt = datetime("created_at")
    
    init {
        uniqueIndex("tenant_usage_unique", tenantId, periodStart)
    }
}

// ============================================================================
// SSO/SAML TABLES (in public schema)
// ============================================================================

/**
 * SsoProviders table - SSO/SAML configuration per tenant.
 *
 * Supports:
 * - SAML 2.0 (Enterprise IdPs)
 * - OAuth 2.0 (Social login)
 * - OIDC (OpenID Connect)
 */
object SsoProviders : LongIdTable("sso_providers") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_provider_tenant")
    val name = varchar("name", 255)
    val type = varchar("type", 50) // SAML, OAUTH, OIDC
    val enabled = bool("enabled").default(true)
    val metadata = text("metadata") // JSON (encrypted sensitive fields)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * SsoSessions table - active SSO sessions.
 *
 * JWT-based session management:
 * - Session token (JWT)
 * - Refresh token (for long sessions)
 * - Expiration tracking
 */
object SsoSessions : LongIdTable("sso_sessions") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_session_tenant")
    val userId = long("user_id").references(TenantUsers.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_session_user")
    val providerId = long("provider_id").references(SsoProviders.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_session_provider")
    val sessionToken = varchar("session_token", 512).uniqueIndex("idx_sso_session_token")
    val refreshToken = varchar("refresh_token", 512).nullable()
    val expiresAt = datetime("expires_at")
    val refreshExpiresAt = datetime("refresh_expires_at").nullable()
    val ipAddress = varchar("ip_address", 50).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt = datetime("created_at")
    val lastActivityAt = datetime("last_activity_at")
}

/**
 * SsoLoginAttempts table - audit log for SSO login attempts.
 *
 * Security & compliance:
 * - Track failed login attempts
 * - Monitor suspicious activity
 * - Audit trail for compliance
 */
object SsoLoginAttempts : LongIdTable("sso_login_attempts") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_attempt_tenant")
    val providerId = long("provider_id").references(SsoProviders.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_sso_attempt_provider")
    val userEmail = varchar("user_email", 255).nullable()
    val success = bool("success")
    val failureReason = text("failure_reason").nullable()
    val ipAddress = varchar("ip_address", 50).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt = datetime("created_at").index("idx_sso_attempt_created")
}

// ============================================================================
// BILLING TABLES (in public schema)
// ============================================================================

/**
 * Subscriptions table - Stripe subscriptions for tenants.
 *
 * Tracks subscription lifecycle:
 * - Active/trial/canceled states
 * - Billing periods
 * - Stripe customer and subscription IDs
 */
object Subscriptions : LongIdTable("subscriptions") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_subscription_tenant")
    val stripeSubscriptionId = varchar("stripe_subscription_id", 255).uniqueIndex("idx_subscription_stripe_id")
    val stripeCustomerId = varchar("stripe_customer_id", 255).index("idx_subscription_customer_id")
    val stripePriceId = varchar("stripe_price_id", 255)
    val status = varchar("status", 50) // ACTIVE, TRIALING, PAST_DUE, UNPAID, CANCELED, INCOMPLETE, INCOMPLETE_EXPIRED
    val currentPeriodStart = datetime("current_period_start")
    val currentPeriodEnd = datetime("current_period_end")
    val cancelAtPeriodEnd = bool("cancel_at_period_end").default(false)
    val canceledAt = datetime("canceled_at").nullable()
    val trialStart = datetime("trial_start").nullable()
    val trialEnd = datetime("trial_end").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * Invoices table - Stripe invoices for subscriptions.
 *
 * Tracks billing history:
 * - Invoice status (draft, open, paid, etc.)
 * - Amount due and paid
 * - Payment dates
 * - Invoice URLs
 */
object Invoices : LongIdTable("invoices") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_invoice_tenant")
    val subscriptionId = long("subscription_id").references(Subscriptions.id, onDelete = ReferenceOption.CASCADE).nullable()
        .index("idx_invoice_subscription")
    val stripeInvoiceId = varchar("stripe_invoice_id", 255).uniqueIndex("idx_invoice_stripe_id")
    val stripeCustomerId = varchar("stripe_customer_id", 255).index("idx_invoice_customer_id")
    val status = varchar("status", 50) // DRAFT, OPEN, PAID, UNCOLLECTIBLE, VOID
    val amountDue = decimal("amount_due", 10, 2)
    val amountPaid = decimal("amount_paid", 10, 2)
    val currency = varchar("currency", 10)
    val periodStart = datetime("period_start")
    val periodEnd = datetime("period_end")
    val dueDate = datetime("due_date").nullable()
    val paidAt = datetime("paid_at").nullable()
    val hostedInvoiceUrl = text("hosted_invoice_url").nullable()
    val invoicePdf = text("invoice_pdf").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * UsageRecords table - tracks metered billing usage.
 *
 * Metered usage for consumption-based pricing:
 * - Evaluations, API requests, etc.
 * - Aggregated and reported to Stripe
 * - Enables pay-as-you-go pricing
 */
object UsageRecords : LongIdTable("usage_records") {
    val tenantId = long("tenant_id").references(Tenants.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_usage_tenant")
    val subscriptionId = long("subscription_id").references(Subscriptions.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_usage_subscription")
    val metricType = varchar("metric_type", 50) // EVALUATIONS, API_REQUESTS, ACTIVE_FLAGS, ACTIVE_ENVIRONMENTS
    val quantity = long("quantity")
    val timestamp = datetime("timestamp").index("idx_usage_timestamp")
    val reportedToStripe = bool("reported_to_stripe").default(false)
    val stripeUsageRecordId = varchar("stripe_usage_record_id", 255).nullable()
    val createdAt = datetime("created_at")
}

// ============================================================================
// AI-POWERED ROLLOUTS TABLES
// ============================================================================

/**
 * MetricDataPoints table - stores metric data points for flags
 *
 * Used for anomaly detection and smart rollouts:
 * - Success rates, error rates, conversion rates
 * - Latency metrics
 * - Custom metrics
 */
object MetricDataPoints : LongIdTable("metric_data_points") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_metric_flag")
    val flagKey = varchar("flag_key", 64).index("idx_metric_flag_key")
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE).nullable()
        .index("idx_metric_segment")
    val variantId = integer("variant_id").references(Variants.id, onDelete = ReferenceOption.CASCADE).nullable()
        .index("idx_metric_variant")
    val variantKey = varchar("variant_key", 255).nullable()
    val metricType = varchar("metric_type", 50) // SUCCESS_RATE, ERROR_RATE, CONVERSION_RATE, LATENCY_MS, RESPONSE_TIME_MS, CUSTOM
    val metricValue = decimal("metric_value", 10, 6)
    val timestamp = long("timestamp").index("idx_metric_timestamp")
    val entityId = varchar("entity_id", 255).nullable()
    val tenantId = varchar("tenant_id", 255).nullable().index("idx_metric_tenant")
    val createdAt = datetime("created_at")
    
    init {
        // Composite index for efficient time-range queries
        index(true, flagId, timestamp)
        index(true, flagKey, timestamp)
    }
}

/**
 * AnomalyAlerts table - stores detected anomalies
 *
 * Triggers automatic actions:
 * - Kill switch (disable flag)
 * - Rollback (decrease rollout)
 * - Alerts (notify team)
 */
object AnomalyAlerts : IntIdTable("anomaly_alerts") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_anomaly_flag")
    val flagKey = varchar("flag_key", 64)
    val variantId = integer("variant_id").references(Variants.id, onDelete = ReferenceOption.CASCADE).nullable()
    val variantKey = varchar("variant_key", 255).nullable()
    val anomalyType = varchar("anomaly_type", 50) // HIGH_ERROR_RATE, LOW_SUCCESS_RATE, HIGH_LATENCY, LOW_CONVERSION_RATE, STATISTICAL_OUTLIER, THRESHOLD_EXCEEDED
    val severity = varchar("severity", 50) // LOW, MEDIUM, HIGH, CRITICAL
    val detectedAt = long("detected_at").index("idx_anomaly_detected")
    val metricType = varchar("metric_type", 50)
    val metricValue = decimal("metric_value", 10, 6)
    val expectedValue = decimal("expected_value", 10, 6)
    val zScore = decimal("z_score", 10, 4)
    val message = text("message")
    val actionTaken = varchar("action_taken", 50).nullable() // NONE, ALERT_SENT, ROLLOUT_PAUSED, ROLLOUT_DECREASED, FLAG_DISABLED, VARIANT_DISABLED
    val actionTakenAt = long("action_taken_at").nullable()
    val resolved = bool("resolved").default(false)
    val resolvedAt = long("resolved_at").nullable()
    val tenantId = varchar("tenant_id", 255).nullable().index("idx_anomaly_tenant")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    
    init {
        // Index for finding unresolved alerts
        index(true, flagId, resolved)
    }
}

/**
 * AnomalyDetectionConfigs table - configuration for anomaly detection per flag
 *
 * Configures thresholds and behavior:
 * - Z-score thresholds
 * - Metric thresholds (error rate, success rate, etc.)
 * - Auto-actions (kill switch, rollback)
 */
object AnomalyDetectionConfigs : IntIdTable("anomaly_detection_configs") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
        .uniqueIndex("idx_anomaly_config_flag")
    val enabled = bool("enabled").default(true)
    val zScoreThreshold = decimal("z_score_threshold", 5, 2).default(3.0.toBigDecimal())
    val errorRateThreshold = decimal("error_rate_threshold", 5, 4).default(0.1.toBigDecimal())
    val successRateThreshold = decimal("success_rate_threshold", 5, 4).default(0.8.toBigDecimal())
    val latencyThresholdMs = decimal("latency_threshold_ms", 10, 2).default(1000.0.toBigDecimal())
    val conversionRateThreshold = decimal("conversion_rate_threshold", 5, 4).default(0.05.toBigDecimal())
    val minSampleSize = integer("min_sample_size").default(100)
    val windowSizeMs = long("window_size_ms").default(300_000) // 5 minutes
    val autoKillSwitch = bool("auto_kill_switch").default(false)
    val autoRollback = bool("auto_rollback").default(false)
    val tenantId = varchar("tenant_id", 255).nullable().index("idx_anomaly_config_tenant")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
}

/**
 * SmartRolloutConfigs table - configuration for smart/automatic rollouts
 *
 * ML-based automatic rollout adjustments:
 * - Target rollout percentage
 * - Increment size and interval
 * - Metric thresholds for continuing
 * - Auto-rollback configuration
 */
object SmartRolloutConfigs : IntIdTable("smart_rollout_configs") {
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_rollout_config_flag")
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_rollout_config_segment")
    val enabled = bool("enabled").default(true)
    val targetRolloutPercent = integer("target_rollout_percent").default(100)
    val currentRolloutPercent = integer("current_rollout_percent").default(0)
    val incrementPercent = integer("increment_percent").default(10)
    val incrementIntervalMs = long("increment_interval_ms").default(3600_000) // 1 hour
    val successRateThreshold = decimal("success_rate_threshold", 5, 4).default(0.95.toBigDecimal())
    val errorRateThreshold = decimal("error_rate_threshold", 5, 4).default(0.05.toBigDecimal())
    val conversionRateThreshold = decimal("conversion_rate_threshold", 5, 4).nullable()
    val minSampleSize = integer("min_sample_size").default(100)
    val autoRollback = bool("auto_rollback").default(true)
    val rollbackOnAnomaly = bool("rollback_on_anomaly").default(true)
    val pauseOnAnomaly = bool("pause_on_anomaly").default(true)
    val notifyOnIncrement = bool("notify_on_increment").default(true)
    val lastIncrementAt = long("last_increment_at").nullable()
    val completedAt = long("completed_at").nullable()
    val status = varchar("status", 50).default("PENDING") // PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED
    val tenantId = varchar("tenant_id", 255).nullable().index("idx_rollout_config_tenant")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    
    init {
        // Unique constraint: one active rollout per flag+segment
        uniqueIndex("smart_rollout_unique", flagId, segmentId)
    }
}

/**
 * SmartRolloutHistory table - history of smart rollout changes
 *
 * Audit trail for rollout decisions:
 * - Previous and new rollout percentages
 * - Reason for change
 * - Metrics at time of decision
 */
object SmartRolloutHistory : IntIdTable("smart_rollout_history") {
    val rolloutConfigId = integer("rollout_config_id").references(SmartRolloutConfigs.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_rollout_history_config")
    val flagId = integer("flag_id").references(Flags.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_rollout_history_flag")
    val segmentId = integer("segment_id").references(Segments.id, onDelete = ReferenceOption.CASCADE)
    val previousPercent = integer("previous_percent")
    val newPercent = integer("new_percent")
    val reason = text("reason")
    val successRate = decimal("success_rate", 5, 4).nullable()
    val errorRate = decimal("error_rate", 5, 4).nullable()
    val sampleSize = integer("sample_size").nullable()
    val timestamp = long("timestamp").index("idx_rollout_history_timestamp")
    val tenantId = varchar("tenant_id", 255).nullable().index("idx_rollout_history_tenant")
    val createdAt = datetime("created_at")
}
