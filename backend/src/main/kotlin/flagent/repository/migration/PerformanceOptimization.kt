package flagent.repository.migration

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Performance optimization migration - adds indices for AI-powered rollouts
 * 
 * Based on load test results and query patterns:
 * - Metrics queries by flag_id + timestamp
 * - Anomaly alerts by flag_id + resolved status
 * - Smart rollout configs by flag_id + status
 */
object PerformanceOptimization {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    /**
     * Apply performance optimizations
     */
    fun apply() {
        transaction {
            logger.info("Applying performance optimizations...")
            
            // Create indices for metrics queries
            exec("""
                CREATE INDEX IF NOT EXISTS idx_metric_flag_timestamp 
                ON metric_data_points(flag_id, timestamp DESC)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_metric_flag_key_timestamp 
                ON metric_data_points(flag_key, timestamp DESC)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_metric_type_timestamp 
                ON metric_data_points(metric_type, timestamp DESC)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_metric_variant 
                ON metric_data_points(flag_id, variant_id, timestamp DESC) 
                WHERE variant_id IS NOT NULL
            """.trimIndent())
            
            // Create indices for anomaly queries
            exec("""
                CREATE INDEX IF NOT EXISTS idx_anomaly_flag_resolved 
                ON anomaly_alerts(flag_id, resolved, detected_at DESC)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_anomaly_severity 
                ON anomaly_alerts(severity, resolved, detected_at DESC)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_anomaly_unresolved 
                ON anomaly_alerts(resolved, detected_at DESC) 
                WHERE resolved = false
            """.trimIndent())
            
            // Create indices for smart rollout queries
            exec("""
                CREATE INDEX IF NOT EXISTS idx_rollout_flag_status 
                ON smart_rollout_configs(flag_id, status, enabled)
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_rollout_active 
                ON smart_rollout_configs(enabled, status, last_increment_at) 
                WHERE enabled = true AND status = 'ACTIVE'
            """.trimIndent())
            
            exec("""
                CREATE INDEX IF NOT EXISTS idx_rollout_history 
                ON smart_rollout_history(rollout_config_id, changed_at DESC)
            """.trimIndent())
            
            // Create indices for anomaly detection configs
            exec("""
                CREATE INDEX IF NOT EXISTS idx_anomaly_config_flag 
                ON anomaly_detection_configs(flag_id, enabled)
            """.trimIndent())
            
            logger.info("Performance optimizations applied successfully")
        }
    }
    
    /**
     * Analyze tables to update statistics
     */
    fun analyze() {
        transaction {
            logger.info("Analyzing tables for query optimizer...")
            
            exec("ANALYZE metric_data_points")
            exec("ANALYZE anomaly_alerts")
            exec("ANALYZE smart_rollout_configs")
            exec("ANALYZE smart_rollout_history")
            exec("ANALYZE anomaly_detection_configs")
            
            logger.info("Table analysis completed")
        }
    }
    
    /**
     * Get index usage statistics (PostgreSQL)
     */
    fun getIndexStats(): List<IndexStat> {
        return transaction {
            val stats = mutableListOf<IndexStat>()
            
            try {
                exec("""
                    SELECT
                        schemaname,
                        tablename,
                        indexname,
                        idx_scan as scans,
                        idx_tup_read as tuples_read,
                        idx_tup_fetch as tuples_fetched
                    FROM pg_stat_user_indexes
                    WHERE schemaname = 'public'
                    AND tablename IN (
                        'metric_data_points',
                        'anomaly_alerts',
                        'smart_rollout_configs',
                        'smart_rollout_history',
                        'anomaly_detection_configs'
                    )
                    ORDER BY idx_scan DESC
                """.trimIndent()) { rs ->
                    while (rs.next()) {
                        stats.add(
                            IndexStat(
                                schema = rs.getString("schemaname"),
                                table = rs.getString("tablename"),
                                index = rs.getString("indexname"),
                                scans = rs.getLong("scans"),
                                tuplesRead = rs.getLong("tuples_read"),
                                tuplesFetched = rs.getLong("tuples_fetched")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to get index stats (PostgreSQL only): ${e.message}")
            }
            
            stats
        }
    }
    
    /**
     * Get table sizes
     */
    fun getTableSizes(): List<TableSize> {
        return transaction {
            val sizes = mutableListOf<TableSize>()
            
            try {
                exec("""
                    SELECT
                        schemaname,
                        tablename,
                        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
                        pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
                    FROM pg_tables
                    WHERE schemaname = 'public'
                    AND tablename IN (
                        'metric_data_points',
                        'anomaly_alerts',
                        'smart_rollout_configs',
                        'smart_rollout_history',
                        'anomaly_detection_configs'
                    )
                    ORDER BY size_bytes DESC
                """.trimIndent()) { rs ->
                    while (rs.next()) {
                        sizes.add(
                            TableSize(
                                schema = rs.getString("schemaname"),
                                table = rs.getString("tablename"),
                                size = rs.getString("size"),
                                sizeBytes = rs.getLong("size_bytes")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to get table sizes (PostgreSQL only): ${e.message}")
            }
            
            sizes
        }
    }
}

data class IndexStat(
    val schema: String,
    val table: String,
    val index: String,
    val scans: Long,
    val tuplesRead: Long,
    val tuplesFetched: Long
)

data class TableSize(
    val schema: String,
    val table: String,
    val size: String,
    val sizeBytes: Long
)
