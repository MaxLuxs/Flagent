package flagent.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import flagent.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
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
        return newSuspendedTransaction(Dispatchers.IO, exposedDatabase!!) {
            block()
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
