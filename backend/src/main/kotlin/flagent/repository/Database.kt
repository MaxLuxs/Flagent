package flagent.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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

            // Create main Flagent tables (in public schema)
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
            
            // Create multi-tenancy tables (in public schema)
            if (AppConfig.multiTenancyEnabled) {
                SchemaUtils.createMissingTablesAndColumns(
                    Tenants,
                    TenantUsers,
                    TenantApiKeys,
                    TenantUsage
                )
                logger.info { "Multi-tenancy tables created" }
                
                // Create SSO tables
                SchemaUtils.createMissingTablesAndColumns(
                    SsoProviders,
                    SsoSessions,
                    SsoLoginAttempts
                )
                logger.info { "SSO/SAML tables created" }
            }
            
            // Create billing tables (if Stripe enabled)
            if (AppConfig.stripeEnabled) {
                SchemaUtils.createMissingTablesAndColumns(
                    Subscriptions,
                    Invoices,
                    UsageRecords
                )
                logger.info { "Billing tables created" }
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
     * Close database connection
     */
    fun close() {
        dataSource?.close()
        logger.info { "Database connection closed" }
    }
}
