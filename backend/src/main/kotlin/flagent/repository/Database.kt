package flagent.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import flagent.api.EnterpriseConfigurator
import flagent.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import flagent.repository.tables.*
import java.util.ServiceLoader

private val logger = KotlinLogging.logger {}

/**
 * Database connection and setup
 * Maps to pkg/entity/db.go from original project
 */
object Database {
    private var dataSource: HikariDataSource? = null
    private var exposedDatabase: Database? = null

    /**
     * Initialize database connection
     * Supports PostgreSQL, MySQL, and SQLite
     */
    fun init() {
        val driver = when (AppConfig.dbDriver) {
            "postgres" -> "org.postgresql.Driver"
            "mysql" -> "com.mysql.cj.jdbc.Driver"
            "sqlite3" -> "org.sqlite.JDBC"
            else -> throw IllegalArgumentException("Unsupported database driver: ${AppConfig.dbDriver}")
        }

        val jdbcUrl = when (AppConfig.dbDriver) {
            "postgres" -> AppConfig.dbConnectionStr
            "mysql" -> AppConfig.dbConnectionStr
            "sqlite3" -> {
                if (AppConfig.dbConnectionStr == ":memory:") {
                    "jdbc:sqlite::memory:"
                } else {
                    "jdbc:sqlite:${AppConfig.dbConnectionStr}"
                }
            }
            else -> throw IllegalArgumentException("Unsupported database driver: ${AppConfig.dbDriver}")
        }

        val config = HikariConfig().apply {
            this.driverClassName = driver
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        }

        dataSource = HikariDataSource(config)
        exposedDatabase = Database.connect(dataSource!!)

        logger.info { "Connected to database: ${AppConfig.dbDriver}" }

        // Run migrations
        runMigrations()
    }

    /**
     * Run database migrations
     * Creates all tables if they don't exist
     */
    private fun runMigrations() {
        transaction(exposedDatabase!!) {
            if (AppConfig.dbConnectionDebug) {
                addLogger(StdOutSqlLogger)
            }

            // Create main Flagent tables (in public schema) - core only
            SchemaUtils.createMissingTablesAndColumns(
                Flags,
                Segments,
                Variants,
                Constraints,
                Distributions,
                Tags,
                FlagsTags,
                FlagSnapshots,
                FlagEntityTypes,
                Users
            )

            // Create AI-powered rollouts tables (always enabled)
            SchemaUtils.createMissingTablesAndColumns(
                MetricDataPoints,
                AnomalyAlerts,
                AnomalyDetectionConfigs,
                SmartRolloutConfigs,
                SmartRolloutHistory
            )
            logger.info { "AI-powered rollouts tables created" }

            // Multi-tenancy, SSO, billing: created by enterprise when present, else by core when config enabled (self-hosted)
            val enterprisePresent = ServiceLoader.load(EnterpriseConfigurator::class.java).toList().isNotEmpty()
            if (!enterprisePresent) {
                if (AppConfig.multiTenancyEnabled) {
                    SchemaUtils.createMissingTablesAndColumns(
                        Tenants,
                        TenantUsers,
                        TenantApiKeys,
                        TenantUsage
                    )
                    SchemaUtils.createMissingTablesAndColumns(
                        SsoProviders,
                        SsoSessions,
                        SsoLoginAttempts
                    )
                    logger.info { "Multi-tenancy and SSO tables created (core)" }
                }
                if (AppConfig.stripeEnabled) {
                    SchemaUtils.createMissingTablesAndColumns(
                        Subscriptions,
                        Invoices,
                        UsageRecords
                    )
                    logger.info { "Billing tables created (core)" }
                }
            }
            logger.info { "Database migrations completed" }
        }
    }

    /**
     * Get database instance
     */
    fun getDatabase(): Database {
        return exposedDatabase ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }

    /**
     * Run a block inside a synchronous transaction (for enterprise migrations).
     */
    fun runBlockingMigrations(block: () -> Unit) {
        transaction(exposedDatabase!!) {
            if (AppConfig.dbConnectionDebug) {
                addLogger(StdOutSqlLogger)
            }
            block()
        }
    }

    /**
     * Run a block inside a synchronous transaction (for enterprise repository operations).
     */
    fun <T> runBlockingTransaction(block: () -> T): T {
        return transaction(exposedDatabase!!) {
            if (AppConfig.dbConnectionDebug) {
                addLogger(StdOutSqlLogger)
            }
            block()
        }
    }

    /**
     * Execute database operation in transaction
     */
    suspend fun <T> transaction(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                block()
            }
        }
    }

    /**
     * Create tenant schema (for enterprise TenantProvisioningService).
     * Called via EnterpriseBackendContext when enterprise module is present.
     */
    suspend fun createTenantSchema(schemaName: String) {
        withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                exec("CREATE SCHEMA IF NOT EXISTS $schemaName")
            }
        }
    }

    /**
     * Run core Flagent table migrations in the given tenant schema.
     */
    suspend fun runTenantSchemaMigrations(schemaName: String) {
        withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                exec("SET search_path TO $schemaName, public")
                SchemaUtils.create(
                    Flags,
                    Segments,
                    Variants,
                    Constraints,
                    Distributions,
                    Tags,
                    FlagsTags,
                    FlagSnapshots,
                    FlagEntityTypes,
                    Users
                )
            }
        }
    }

    /**
     * Drop tenant schema (for enterprise tenant deletion).
     */
    suspend fun dropTenantSchema(schemaName: String) {
        withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                exec("DROP SCHEMA IF EXISTS $schemaName CASCADE")
            }
        }
    }

    /**
     * Close database connection
     */
    fun close() {
        dataSource?.close()
        logger.info { "Database connection closed" }
    }
}
