package flagent.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import flagent.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import flagent.repository.tables.AnalyticsEvents
import flagent.repository.tables.Constraints
import flagent.repository.tables.Distributions
import flagent.repository.tables.EvaluationEvents
import flagent.repository.tables.FlagEntityTypes
import flagent.repository.tables.FlagSnapshots
import flagent.repository.tables.Flags
import flagent.repository.tables.FlagsTags
import flagent.repository.tables.Segments
import flagent.repository.tables.Tags
import flagent.repository.tables.Users
import flagent.repository.tables.Variants
import flagent.repository.tables.Webhooks

private val logger = KotlinLogging.logger {}

/**
 * Database connection and initialization
 */
object Database {
    private var dataSource: HikariDataSource? = null
    private var exposedDatabase: Database? = null

    /**
     * Initialize database connection
     * Supports PostgreSQL, MySQL, and SQLite
     */
    fun init() {
        val dbDriver = when (AppConfig.dbDriver) {
            "postgres", "postgresql" -> "postgres"
            else -> AppConfig.dbDriver
        }
        val driver = when (dbDriver) {
            "postgres" -> "org.postgresql.Driver"
            "mysql" -> "com.mysql.cj.jdbc.Driver"
            "sqlite3" -> "org.sqlite.JDBC"
            else -> throw IllegalArgumentException("Unsupported database driver: ${AppConfig.dbDriver}")
        }

        val jdbcUrl = when (dbDriver) {
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

        val useMemoryDb = dbDriver == "sqlite3" && AppConfig.dbConnectionStr == ":memory:"
        val config = HikariConfig().apply {
            this.driverClassName = driver
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = if (useMemoryDb) 1 else 10
            minimumIdle = if (useMemoryDb) 1 else 2
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

            // Create core Flagent tables only (enterprise creates its own tables when present)
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
                Webhooks,
                Users,
                EvaluationEvents,
                AnalyticsEvents
            )
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

    private fun isSqlite(): Boolean {
        val dbDriver = when (AppConfig.dbDriver) {
            "postgres", "postgresql" -> "postgres"
            else -> AppConfig.dbDriver
        }
        return dbDriver == "sqlite3"
    }

    /**
     * Create tenant schema (for enterprise TenantProvisioningService).
     * Called via EnterpriseBackendContext when enterprise module is present.
     * No-op for SQLite (no schema support); tenants share single DB via tenant_id.
     */
    suspend fun createTenantSchema(schemaName: String) {
        if (isSqlite()) return
        withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                exec("CREATE SCHEMA IF NOT EXISTS $schemaName")
            }
        }
    }

    /**
     * Run core Flagent table migrations in the given tenant schema.
     * No-op for SQLite (tables already created in default namespace).
     */
    suspend fun runTenantSchemaMigrations(schemaName: String) {
        if (isSqlite()) return
        withContext(Dispatchers.IO) {
            suspendTransaction(exposedDatabase!!) {
                exec("SET search_path TO $schemaName, public")
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
            }
        }
    }

    /**
     * Drop tenant schema (for enterprise tenant deletion).
     * No-op for SQLite (no schema support).
     */
    suspend fun dropTenantSchema(schemaName: String) {
        if (isSqlite()) return
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
