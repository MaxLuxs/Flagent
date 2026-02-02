package flagent.config

import flagent.api.constants.ApiConstants
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Application configuration loaded from environment variables
 */
object AppConfig {
    // Server
    val host: String = System.getenv("HOST") ?: "localhost"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 18000

    // Logging
    val logrusLevel: String = System.getenv("FLAGENT_LOGRUS_LEVEL") ?: "info"
    val logrusFormat: String = System.getenv("FLAGENT_LOGRUS_FORMAT") ?: "text"
    val pprofEnabled: Boolean = System.getenv("FLAGENT_PPROF_ENABLED")?.toBoolean() ?: false

    // Middleware
    val middlewareVerboseLoggerEnabled: Boolean =
        System.getenv("FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_ENABLED")?.toBoolean() ?: true
    val middlewareVerboseLoggerExcludeURLs: List<String> =
        System.getenv("FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_EXCLUDE_URLS")?.split(",")
            ?.filter { it.isNotBlank() } ?: emptyList()
    val middlewareGzipEnabled: Boolean =
        System.getenv("FLAGENT_MIDDLEWARE_GZIP_ENABLED")?.toBoolean() ?: true

    // Rate Limiting
    val rateLimiterPerFlagPerSecondConsoleLogging: Int =
        System.getenv("FLAGENT_RATELIMITER_PERFLAG_PERSECOND_CONSOLE_LOGGING")?.toIntOrNull() ?: 100

    // Evaluation
    val evalDebugEnabled: Boolean =
        System.getenv("FLAGENT_EVAL_DEBUG_ENABLED")?.toBoolean() ?: true
    val evalLoggingEnabled: Boolean =
        System.getenv("FLAGENT_EVAL_LOGGING_ENABLED")?.toBoolean() ?: true
    val evalCacheRefreshTimeout: Duration =
        System.getenv("FLAGENT_EVALCACHE_REFRESHTIMEOUT")?.let { parseDuration(it) } ?: 59.seconds
    val evalCacheRefreshInterval: Duration =
        System.getenv("FLAGENT_EVALCACHE_REFRESHINTERVAL")?.let { parseDuration(it) } ?: 3.seconds

    // Database
    val dbDriver: String = System.getenv("FLAGENT_DB_DBDRIVER") ?: "sqlite3"

    val evalOnlyMode: Boolean =
        System.getenv("FLAGENT_EVAL_ONLY_MODE")?.toBoolean() ?: (dbDriver in listOf(
            "json_file",
            "json_http"
        ))
    val dbConnectionStr: String = System.getenv("FLAGENT_DB_DBCONNECTIONSTR") ?: "flagent.sqlite"
    val dbConnectionDebug: Boolean =
        System.getenv("FLAGENT_DB_DBCONNECTION_DEBUG")?.toBoolean() ?: true
    val dbConnectionRetryAttempts: UInt =
        System.getenv("FLAGENT_DB_DBCONNECTION_RETRY_ATTEMPTS")?.toUIntOrNull() ?: 9u
    val dbConnectionRetryDelay: Duration =
        System.getenv("FLAGENT_DB_DBCONNECTION_RETRY_DELAY")?.let { parseDuration(it) }
            ?: 100.milliseconds

    // CORS
    val corsEnabled: Boolean = System.getenv("FLAGENT_CORS_ENABLED")?.toBoolean() ?: true
    val corsAllowCredentials: Boolean =
        System.getenv("FLAGENT_CORS_ALLOW_CREDENTIALS")?.toBoolean() ?: true
    val corsAllowedHeaders: List<String> =
        System.getenv("FLAGENT_CORS_ALLOWED_HEADERS")?.split(",")?.map { it.trim() }
            ?: listOf(
                "Origin",
                "Accept",
                "Content-Type",
                "X-Requested-With",
                "Authorization",
                "Time_Zone"
            )
    val corsAllowedMethods: List<String> =
        System.getenv("FLAGENT_CORS_ALLOWED_METHODS")?.split(",")?.map { it.trim() }
            ?: listOf("GET", "POST", "PUT", "DELETE", "PATCH")
    val corsAllowedOrigins: List<String> =
        System.getenv("FLAGENT_CORS_ALLOWED_ORIGINS")?.split(",")?.map { it.trim() }
            ?: listOf("*")
    val corsExposedHeaders: List<String> =
        System.getenv("FLAGENT_CORS_EXPOSED_HEADERS")?.split(",")?.map { it.trim() }
            ?: listOf("WWW-Authenticate")
    val corsMaxAge: Int = System.getenv("FLAGENT_CORS_MAX_AGE")?.toIntOrNull() ?: 600

    // Sentry
    val sentryEnabled: Boolean = System.getenv("FLAGENT_SENTRY_ENABLED")?.toBoolean() ?: false
    val sentryDSN: String = System.getenv("FLAGENT_SENTRY_DSN") ?: ""
    val sentryEnvironment: String = System.getenv("FLAGENT_SENTRY_ENVIRONMENT") ?: ""

    // New Relic
    val newRelicEnabled: Boolean = System.getenv("FLAGENT_NEWRELIC_ENABLED")?.toBoolean() ?: false
    val newRelicDistributedTracingEnabled: Boolean =
        System.getenv("FLAGENT_NEWRELIC_DISTRIBUTED_TRACING_ENABLED")?.toBoolean() ?: false
    val newRelicAppName: String = System.getenv("FLAGENT_NEWRELIC_NAME") ?: "flagent"
    val newRelicKey: String = System.getenv("FLAGENT_NEWRELIC_KEY") ?: ""

    // StatsD
    val statsdEnabled: Boolean = System.getenv("FLAGENT_STATSD_ENABLED")?.toBoolean() ?: false
    val statsdHost: String = System.getenv("FLAGENT_STATSD_HOST") ?: "127.0.0.1"
    val statsdPort: String = System.getenv("FLAGENT_STATSD_PORT") ?: "8125"
    val statsdPrefix: String = System.getenv("FLAGENT_STATSD_PREFIX") ?: "flagent."
    val statsdAPMEnabled: Boolean = System.getenv("FLAGENT_STATSD_APM_ENABLED")?.toBoolean() ?: false
    val statsdAPMPort: String = System.getenv("FLAGENT_STATSD_APM_PORT") ?: "8126"
    val statsdAPMServiceName: String = System.getenv("FLAGENT_STATSD_APM_SERVICE_NAME") ?: "flagent"

    // Prometheus
    val prometheusEnabled: Boolean = System.getenv("FLAGENT_PROMETHEUS_ENABLED")?.toBoolean() ?: false
    val prometheusPath: String = System.getenv("FLAGENT_PROMETHEUS_PATH") ?: "/metrics"
    val prometheusIncludeLatencyHistogram: Boolean =
        System.getenv("FLAGENT_PROMETHEUS_INCLUDE_LATENCY_HISTOGRAM")?.toBoolean() ?: false

    // Data Recorder
    val recorderEnabled: Boolean = System.getenv("FLAGENT_RECORDER_ENABLED")?.toBoolean() ?: false
    val recorderType: String = System.getenv("FLAGENT_RECORDER_TYPE") ?: "kafka"
    val recorderFrameOutputMode: String =
        System.getenv("FLAGENT_RECORDER_FRAME_OUTPUT_MODE") ?: "payload_string"

    // Kafka Recorder
    val recorderKafkaVersion: String = System.getenv("FLAGENT_RECORDER_KAFKA_VERSION") ?: "0.8.2.0"
    val recorderKafkaBrokers: String = System.getenv("FLAGENT_RECORDER_KAFKA_BROKERS") ?: ":9092"
    val recorderKafkaCompressionCodec: Int =
        System.getenv("FLAGENT_RECORDER_KAFKA_COMPRESSION_CODEC")?.toIntOrNull() ?: 0
    val recorderKafkaCertFile: String = System.getenv("FLAGENT_RECORDER_KAFKA_CERTFILE") ?: ""
    val recorderKafkaKeyFile: String = System.getenv("FLAGENT_RECORDER_KAFKA_KEYFILE") ?: ""
    val recorderKafkaCAFile: String = System.getenv("FLAGENT_RECORDER_KAFKA_CAFILE") ?: ""
    val recorderKafkaVerifySSL: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_VERIFYSSL")?.toBoolean() ?: false
    val recorderKafkaSimpleSSL: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_SIMPLE_SSL")?.toBoolean() ?: false
    val recorderKafkaSASLUsername: String =
        System.getenv("FLAGENT_RECORDER_KAFKA_SASL_USERNAME") ?: ""
    val recorderKafkaSASLPassword: String =
        System.getenv("FLAGENT_RECORDER_KAFKA_SASL_PASSWORD") ?: ""
    val recorderKafkaVerbose: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_VERBOSE")?.toBoolean() ?: true
    val recorderKafkaTopic: String = System.getenv("FLAGENT_RECORDER_KAFKA_TOPIC") ?: "flagent-records"
    val recorderKafkaPartitionKeyEnabled: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_PARTITION_KEY_ENABLED")?.toBoolean() ?: true
    val recorderKafkaRetryMax: Int =
        System.getenv("FLAGENT_RECORDER_KAFKA_RETRYMAX")?.toIntOrNull() ?: 5
    val recorderKafkaMaxOpenReqs: Int =
        System.getenv("FLAGENT_RECORDER_KAFKA_MAXOPENREQUESTS")?.toIntOrNull() ?: 5
    val recorderKafkaRequiredAcks: Int =
        System.getenv("FLAGENT_RECORDER_KAFKA_REQUIRED_ACKS")?.toIntOrNull() ?: 1
    val recorderKafkaIdempotent: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_IDEMPOTENT")?.toBoolean() ?: false
    val recorderKafkaFlushFrequency: Duration =
        System.getenv("FLAGENT_RECORDER_KAFKA_FLUSHFREQUENCY")?.let { parseDuration(it) }
            ?: 500.milliseconds
    val recorderKafkaEncrypted: Boolean =
        System.getenv("FLAGENT_RECORDER_KAFKA_ENCRYPTED")?.toBoolean() ?: false
    val recorderKafkaEncryptionKey: String =
        System.getenv("FLAGENT_RECORDER_KAFKA_ENCRYPTION_KEY") ?: ""

    // Kinesis Recorder
    val recorderKinesisStreamName: String =
        System.getenv("FLAGENT_RECORDER_KINESIS_STREAM_NAME") ?: "flagent-records"
    val recorderKinesisBacklogCount: Int =
        System.getenv("FLAGENT_RECORDER_KINESIS_BACKLOG_COUNT")?.toIntOrNull() ?: 500
    val recorderKinesisMaxConnections: Int =
        System.getenv("FLAGENT_RECORDER_KINESIS_MAX_CONNECTIONS")?.toIntOrNull() ?: 24
    val recorderKinesisFlushInterval: Duration =
        System.getenv("FLAGENT_RECORDER_KINESIS_FLUSH_INTERVAL")?.let { parseDuration(it) }
            ?: 5.seconds
    val recorderKinesisBatchCount: Int =
        System.getenv("FLAGENT_RECORDER_KINESIS_BATCH_COUNT")?.toIntOrNull() ?: 500
    val recorderKinesisBatchSize: Int =
        System.getenv("FLAGENT_RECORDER_KINESIS_BATCH_SIZE")?.toIntOrNull() ?: 0
    val recorderKinesisAggregateBatchCount: Long =
        System.getenv("FLAGENT_RECORDER_KINESIS_AGGREGATE_BATCH_COUNT")?.toLongOrNull() ?: 4294967295L
    val recorderKinesisAggregateBatchSize: Int =
        System.getenv("FLAGENT_RECORDER_KINESIS_AGGREGATE_BATCH_SIZE")?.toIntOrNull() ?: 51200
    val recorderKinesisVerbose: Boolean =
        System.getenv("FLAGENT_RECORDER_KINESIS_VERBOSE")?.toBoolean() ?: false

    // PubSub Recorder
    val recorderPubsubProjectID: String = System.getenv("FLAGENT_RECORDER_PUBSUB_PROJECT_ID") ?: ""
    val recorderPubsubTopicName: String =
        System.getenv("FLAGENT_RECORDER_PUBSUB_TOPIC_NAME") ?: "flagent-records"
    val recorderPubsubKeyFile: String = System.getenv("FLAGENT_RECORDER_PUBSUB_KEYFILE") ?: ""
    val recorderPubsubVerbose: Boolean =
        System.getenv("FLAGENT_RECORDER_PUBSUB_VERBOSE")?.toBoolean() ?: false
    val recorderPubsubVerboseCancelTimeout: Duration =
        System.getenv("FLAGENT_RECORDER_PUBSUB_VERBOSE_CANCEL_TIMEOUT")?.let { parseDuration(it) }
            ?: 5.seconds

    // Firebase Remote Config sync
    val firebaseRcSyncEnabled: Boolean =
        System.getenv("FLAGENT_FIREBASE_RC_SYNC_ENABLED")?.toBoolean() ?: false
    val firebaseRcProjectId: String = System.getenv("FLAGENT_FIREBASE_RC_PROJECT_ID") ?: ""
    val firebaseRcCredentialsJson: String = System.getenv("FLAGENT_FIREBASE_RC_CREDENTIALS_JSON") ?: ""
    val firebaseRcCredentialsFile: String =
        System.getenv("FLAGENT_FIREBASE_RC_CREDENTIALS_FILE") ?: ""
    val firebaseRcSyncInterval: Duration =
        System.getenv("FLAGENT_FIREBASE_RC_SYNC_INTERVAL")?.let { parseDuration(it) }
            ?: 5.seconds
    val firebaseRcParameterPrefix: String =
        System.getenv("FLAGENT_FIREBASE_RC_PARAMETER_PREFIX") ?: ""

    // Firebase Analytics (GA4 Measurement Protocol)
    val firebaseAnalyticsEnabled: Boolean =
        System.getenv("FLAGENT_FIREBASE_ANALYTICS_ENABLED")?.toBoolean() ?: false
    val firebaseAnalyticsApiSecret: String =
        System.getenv("FLAGENT_FIREBASE_ANALYTICS_API_SECRET") ?: ""
    val firebaseAnalyticsMeasurementId: String =
        System.getenv("FLAGENT_FIREBASE_ANALYTICS_MEASUREMENT_ID") ?: ""
    val firebaseAnalyticsAppInstanceIdKey: String =
        System.getenv("FLAGENT_FIREBASE_ANALYTICS_APP_INSTANCE_ID_KEY") ?: "app_instance_id"
    val firebaseAnalyticsClientIdKey: String =
        System.getenv("FLAGENT_FIREBASE_ANALYTICS_CLIENT_ID_KEY") ?: "client_id"

    // JWT Auth
    val jwtAuthEnabled: Boolean = System.getenv("FLAGENT_JWT_AUTH_ENABLED")?.toBoolean() ?: false
    val jwtAuthDebug: Boolean = System.getenv("FLAGENT_JWT_AUTH_DEBUG")?.toBoolean() ?: false
    val jwtAuthPrefixWhitelistPaths: List<String> =
        System.getenv("FLAGENT_JWT_AUTH_WHITELIST_PATHS")?.split(",")?.map { it.trim() }
            ?: listOf("${ApiConstants.API_BASE_PATH}/health", "${ApiConstants.API_BASE_PATH}/evaluation", "/static")
    val jwtAuthExactWhitelistPaths: List<String> =
        System.getenv("FLAGENT_JWT_AUTH_EXACT_WHITELIST_PATHS")?.split(",")?.map { it.trim() }
            ?: listOf("", "/")
    val jwtAuthCookieTokenName: String =
        System.getenv("FLAGENT_JWT_AUTH_COOKIE_TOKEN_NAME") ?: "access_token"
    val jwtAuthSecret: String = System.getenv("FLAGENT_JWT_AUTH_SECRET") ?: ""
    val jwtAuthNoTokenStatusCode: Int =
        System.getenv("FLAGENT_JWT_AUTH_NO_TOKEN_STATUS_CODE")?.toIntOrNull() ?: 307
    val jwtAuthNoTokenRedirectURL: String =
        System.getenv("FLAGENT_JWT_AUTH_NO_TOKEN_REDIRECT_URL") ?: ""
    val jwtAuthUserProperty: String = System.getenv("FLAGENT_JWT_AUTH_USER_PROPERTY") ?: "flagent_user"
    val jwtAuthUserClaim: String = System.getenv("FLAGENT_JWT_AUTH_USER_CLAIM") ?: "sub"
    val jwtAuthSigningMethod: String = System.getenv("FLAGENT_JWT_AUTH_SIGNING_METHOD") ?: "HS256"

    // Header Auth
    val headerAuthEnabled: Boolean =
        System.getenv("FLAGENT_HEADER_AUTH_ENABLED")?.toBoolean() ?: false
    val headerAuthUserField: String = System.getenv("FLAGENT_HEADER_AUTH_USER_FIELD") ?: "X-Email"

    // Cookie Auth
    val cookieAuthEnabled: Boolean =
        System.getenv("FLAGENT_COOKIE_AUTH_ENABLED")?.toBoolean() ?: false
    val cookieAuthUserField: String =
        System.getenv("FLAGENT_COOKIE_AUTH_USER_FIELD") ?: "CF_Authorization"
    val cookieAuthUserFieldJWTClaim: String =
        System.getenv("FLAGENT_COOKIE_AUTH_USER_FIELD_JWT_CLAIM") ?: "email"

    // Basic Auth
    val basicAuthEnabled: Boolean = System.getenv("FLAGENT_BASIC_AUTH_ENABLED")?.toBoolean() ?: false
    val basicAuthUsername: String = System.getenv("FLAGENT_BASIC_AUTH_USERNAME") ?: ""
    val basicAuthPassword: String = System.getenv("FLAGENT_BASIC_AUTH_PASSWORD") ?: ""
    val basicAuthPrefixWhitelistPaths: List<String> =
        System.getenv("FLAGENT_BASIC_AUTH_WHITELIST_PATHS")?.split(",")?.map { it.trim() }
            ?: listOf("${ApiConstants.API_BASE_PATH}/health", "${ApiConstants.API_BASE_PATH}/flags", "${ApiConstants.API_BASE_PATH}/evaluation")
    val basicAuthExactWhitelistPaths: List<String> =
        System.getenv("FLAGENT_BASIC_AUTH_EXACT_WHITELIST_PATHS")?.split(",")?.map { it.trim() }
            ?: emptyList()

    // Web Prefix
    val webPrefix: String = System.getenv("FLAGENT_WEB_PREFIX") ?: ""

    // Admin Auth (for POST /auth/login and /admin/* protection when used with enterprise)
    val adminAuthEnabled: Boolean = System.getenv("FLAGENT_ADMIN_AUTH_ENABLED")?.toBoolean() ?: false
    val adminEmail: String = System.getenv("FLAGENT_ADMIN_EMAIL") ?: ""
    val adminPassword: String = System.getenv("FLAGENT_ADMIN_PASSWORD") ?: ""
    val adminPasswordHash: String = System.getenv("FLAGENT_ADMIN_PASSWORD_HASH") ?: ""
    val adminApiKey: String = System.getenv("FLAGENT_ADMIN_API_KEY") ?: ""

    // Enterprise (multi-tenancy, Stripe, SSO, Slack) is configured in flagent-enterprise via EnterpriseConfig

    private fun parseDuration(s: String): Duration {
        return when {
            s.endsWith("ms") -> s.removeSuffix("ms").toLongOrNull()?.milliseconds ?: 0.seconds
            s.endsWith("s") -> s.removeSuffix("s").toLongOrNull()?.seconds ?: 0.seconds
            s.endsWith("m") -> (s.removeSuffix("m").toLongOrNull()?.let { it * 60 } ?: 0).seconds
            s.endsWith("h") -> (s.removeSuffix("h").toLongOrNull()?.let { it * 3600 } ?: 0).seconds
            else -> s.toLongOrNull()?.milliseconds ?: 0.seconds
        }
    }
}
