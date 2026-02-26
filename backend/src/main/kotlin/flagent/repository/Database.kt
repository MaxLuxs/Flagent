package flagent.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import flagent.config.AppConfig
import flagent.config.DatabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import flagent.repository.tables.AnalyticsEvents
import flagent.repository.tables.CrashReports
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
     * Initialize database for tests with an explicit JDBC URL (e.g. from Testcontainers).
     * Runs Flyway for PostgreSQL; uses SchemaUtils for other drivers.
     * Safe to call multiple times: closes existing connection first.
     */
    fun initForTests(jdbcUrl: String, driverClassName: String = "org.postgresql.Driver") {
        close()
        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = 2
            minimumIdle = 1
            connectionTimeout = 30000
        }
        dataSource = HikariDataSource(config)
        exposedDatabase = Database.connect(dataSource!!)
        logger.info { "Test database connected: $driverClassName" }
        val isPostgres = driverClassName.contains("postgresql", ignoreCase = true)
        if (isPostgres) {
            val flyway = Flyway.configure()
                .dataSource(dataSource!!)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
            flyway.migrate()
            logger.info { "Flyway test migrations completed" }
        } else {
            transaction(exposedDatabase!!) {
                if (AppConfig.dbConnectionDebug) addLogger(StdOutSqlLogger)
                SchemaUtils.createMissingTablesAndColumns(
                    Flags, Segments, Variants, Constraints, Distributions,
                    Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                    Users, EvaluationEvents, AnalyticsEvents, CrashReports
                )
            }
        }
    }

    /**
     * Initialize database connection
     * Supports PostgreSQL, MySQL, and SQLite
     * Safe to call multiple times (e.g. in tests): if already initialised (e.g. by [initForTests]), no-op.
     */
    fun init() {
        if (exposedDatabase != null) return
        close()
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
                    // busy_timeout: wait up to 30s on lock instead of immediate SQLITE_BUSY
                    val base = "jdbc:sqlite:${AppConfig.dbConnectionStr}"
                    val sep = if (AppConfig.dbConnectionStr.contains("?")) "&" else "?"
                    "$base${sep}busy_timeout=30000"
                }
            }
            else -> throw IllegalArgumentException("Unsupported database driver: ${AppConfig.dbDriver}")
        }

        // SQLite uses file-level locking: only one writer. Pool must be 1 for file DB.
        val useSqlite = dbDriver == "sqlite3"
        val config = HikariConfig().apply {
            this.driverClassName = driver
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = if (useSqlite) 1 else DatabaseConfig.getPoolSize()
            minimumIdle = if (useSqlite) 1 else (DatabaseConfig.hikariConfig["minimumIdle"] as? Int ?: 10)
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        }

        val maxAttempts = 1 + AppConfig.dbConnectionRetryAttempts.toInt()
        var lastException: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                dataSource = HikariDataSource(config)
                exposedDatabase = Database.connect(dataSource!!)
                logger.info { "Connected to database: ${AppConfig.dbDriver}" }
                runMigrations(jdbcUrl, dbDriver)
                return
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxAttempts) {
                    logger.warn(e) { "Database connection attempt $attempt/$maxAttempts failed, retrying in ${AppConfig.dbConnectionRetryDelay}..." }
                    Thread.sleep(AppConfig.dbConnectionRetryDelay.inWholeMilliseconds)
                }
            }
        }
        throw lastException ?: IllegalStateException("Database init failed")
    }

    /**
     * Run database migrations.
     * Uses Flyway for postgres/mysql when enabled; falls back to SchemaUtils for SQLite.
     */
    private fun runMigrations(jdbcUrl: String, dbDriver: String) {
        if (AppConfig.flywayEnabled && dbDriver == "postgres") {
            val flyway = Flyway.configure()
                .dataSource(dataSource!!)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
            val result = flyway.migrate()
            logger.info { "Flyway migrations completed: ${result.migrationsExecuted} applied" }
        } else {
            transaction(exposedDatabase!!) {
                if (AppConfig.dbConnectionDebug) {
                    addLogger(StdOutSqlLogger)
                }
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
                    AnalyticsEvents,
                    CrashReports
                )
                logger.info { "Database migrations completed (SchemaUtils)" }
            }
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
        dataSource = null
        exposedDatabase = null
        logger.info { "Database connection closed" }
    }
}
