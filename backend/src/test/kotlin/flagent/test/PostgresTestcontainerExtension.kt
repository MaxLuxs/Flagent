package flagent.test

import flagent.repository.Database
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * JUnit 5 extension: starts a PostgreSQL Testcontainer (when Docker is available) and initialises [Database].
 * If Docker is not available, falls back to Database.init() (uses FLAGENT_DB_* env, e.g. sqlite :memory:).
 * Use @ExtendWith(PostgresTestcontainerExtension::class) on test classes that need a real DB.
 * Do not call Database.init() in @BeforeTest when using this extension.
 */
class PostgresTestcontainerExtension : BeforeAllCallback, AfterAllCallback {

    companion object {
        private const val IMAGE = "postgres:15-alpine"
        private var container: PostgreSQLContainer<*>? = null
        private var initCount = 0

        @Synchronized
        fun getOrCreateContainer(): PostgreSQLContainer<*>? {
            if (container != null && container!!.isRunning) return container
            return try {
                PostgreSQLContainer(DockerImageName.parse(IMAGE))
                    .apply {
                        withDatabaseName("flagent_test")
                        withUsername("test")
                        withPassword("test")
                        withStartupTimeout(java.time.Duration.ofSeconds(120))
                        start()
                    }
                    .also {
                        container = it
                        initCount = 0
                    }
            } catch (_: Exception) {
                null
            }
        }

        @Synchronized
        fun release() {
            initCount--
            if (initCount <= 0) {
                try {
                    Database.close()
                } catch (_: Exception) { }
                container?.stop()
                container = null
            }
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        initCount++
        val c = getOrCreateContainer()
        if (c != null) {
            Database.initForTests(c.jdbcUrl, c.driverClassName)
        } else {
            Database.init()
        }
    }

    override fun afterAll(context: ExtensionContext) {
        release()
    }
}
