package flagent.repository.migration

import flagent.repository.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PerformanceOptimizationTest {

    @BeforeTest
    fun setUpDatabase() {
        // Use in-memory SQLite for fast, isolated tests
        Database.initForTests("jdbc:sqlite::memory:", "org.sqlite.JDBC")

        // Create only the minimal tables needed for index creation to succeed
        transaction(Database.getDatabase()) {
            exec(
                """
                CREATE TABLE IF NOT EXISTS metric_data_points (
                    flag_id INTEGER,
                    flag_key TEXT,
                    metric_type TEXT,
                    variant_id INTEGER,
                    timestamp INTEGER
                )
                """.trimIndent()
            )

            exec(
                """
                CREATE TABLE IF NOT EXISTS anomaly_alerts (
                    flag_id INTEGER,
                    resolved BOOLEAN,
                    detected_at INTEGER,
                    severity TEXT
                )
                """.trimIndent()
            )

            exec(
                """
                CREATE TABLE IF NOT EXISTS smart_rollout_configs (
                    flag_id INTEGER,
                    status TEXT,
                    enabled BOOLEAN,
                    last_increment_at INTEGER
                )
                """.trimIndent()
            )

            exec(
                """
                CREATE TABLE IF NOT EXISTS smart_rollout_history (
                    rollout_config_id INTEGER,
                    changed_at INTEGER
                )
                """.trimIndent()
            )

            exec(
                """
                CREATE TABLE IF NOT EXISTS anomaly_detection_configs (
                    flag_id INTEGER,
                    enabled BOOLEAN
                )
                """.trimIndent()
            )
        }
    }

    @AfterTest
    fun tearDownDatabase() {
        Database.close()
    }

    @Test
    fun apply_createsIndices_withoutThrowing() {
        PerformanceOptimization.apply()
    }

    @Test
    fun analyze_runsAnalyzeStatements_withoutThrowing() {
        PerformanceOptimization.analyze()
    }

    @Test
    fun getIndexStats_returnsList_evenWhenPgViewsMissing() {
        val stats = PerformanceOptimization.getIndexStats()
        assertNotNull(stats)
        // On SQLite / non-Postgres this will be empty, but should not throw
        assertTrue(stats.isEmpty())
    }

    @Test
    fun getTableSizes_returnsList_evenWhenPgTablesMissing() {
        val sizes = PerformanceOptimization.getTableSizes()
        assertNotNull(sizes)
        // On SQLite / non-Postgres this will be empty, but should not throw
        assertTrue(sizes.isEmpty())
    }
}

