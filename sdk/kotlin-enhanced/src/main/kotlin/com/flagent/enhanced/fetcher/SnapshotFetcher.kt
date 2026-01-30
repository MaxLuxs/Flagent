package com.flagent.enhanced.fetcher

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.model.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * SnapshotFetcher - fetches flag configurations from server.
 *
 * Provides bootstrap and refresh mechanisms for downloading flag snapshots
 * from the Flagent server.
 *
 * @param exportApi Export API client for bulk flag data
 * @param flagApi Flag API client for individual flag operations
 *
 * @example
 * ```
 * val fetcher = SnapshotFetcher(exportApi, flagApi)
 * val snapshot = fetcher.fetchSnapshot()
 * ```
 */
class SnapshotFetcher(
    private val exportApi: ExportApi,
    private val flagApi: FlagApi
) {
    
    /**
     * Fetch complete flag snapshot from server.
     *
     * Downloads all flags with their segments, variants, constraints, and distributions.
     * Uses the export API for efficient bulk data transfer.
     *
     * @param ttlMs TTL for the snapshot in milliseconds (default 5 minutes)
     * @return Flag snapshot ready for local evaluation
     * @throws Exception if fetch fails
     */
    suspend fun fetchSnapshot(ttlMs: Long = 300000): FlagSnapshot = withContext(Dispatchers.IO) {
        try {
            // Fetch snapshot from export API (more efficient)
            val response = exportApi.getExportEvalCacheJSON()
            val body = response.body()
            val json = mapToJsonObject(body)
            val flags = parseExportJson(json)
            
            FlagSnapshot(
                flags = flags,
                revision = generateRevision(),
                fetchedAt = System.currentTimeMillis(),
                ttlMs = ttlMs
            )
        } catch (e: Exception) {
            // Fallback to individual flag fetch if export fails
            fetchSnapshotFallback(ttlMs)
        }
    }

    /**
     * Fallback method to fetch flags individually.
     *
     * Used when export API is unavailable or fails.
     */
    private suspend fun fetchSnapshotFallback(ttlMs: Long): FlagSnapshot {
        val response = flagApi.findFlags(
            limit = 1000,
            offset = null,
            enabled = null,
            description = null,
            key = null,
            descriptionLike = null,
            preload = true,
            deleted = false,
            tags = null
        )
        val flagsList = response.body()
        
        val flags = flagsList.mapNotNull { flagDto ->
            val flagId = flagDto.id
            
            val segments = flagDto.segments?.map { segmentDto ->
                LocalSegment(
                    id = segmentDto.id,
                    rank = (segmentDto.rank).toInt(),
                    rolloutPercent = (segmentDto.rolloutPercent).toInt(),
                    constraints = segmentDto.constraints?.map { constraintDto ->
                        LocalConstraint(
                            id = constraintDto.id,
                            property = constraintDto.`property`,
                            operator = constraintDto.`operator`.value,
                            value = constraintDto.`value`
                        )
                    } ?: emptyList(),
                    distributions = segmentDto.distributions?.map { distDto ->
                        LocalDistribution(
                            id = distDto.id,
                            variantID = distDto.variantID,
                            variantKey = distDto.variantKey ?: "",
                            percent = distDto.percent.toInt()
                        )
                    } ?: emptyList(),
                    description = segmentDto.description
                )
            } ?: emptyList()
            
            val variants = flagDto.variants?.map { variantDto ->
                LocalVariant(
                    id = variantDto.id,
                    key = variantDto.key,
                    attachment = variantDto.attachment
                )
            } ?: emptyList()
            
            flagId to LocalFlag(
                id = flagId,
                key = flagDto.key,
                enabled = flagDto.enabled,
                segments = segments,
                variants = variants,
                description = flagDto.description,
                entityType = flagDto.entityType,
                updatedAt = null
            )
        }.toMap()
        
        return FlagSnapshot(
            flags = flags,
            revision = generateRevision(),
            fetchedAt = System.currentTimeMillis(),
            ttlMs = ttlMs
        )
    }

    /**
     * Parse export JSON format into flag map.
     *
     * The export format is optimized for evaluation cache.
     */
    private fun parseExportJson(json: JsonObject): Map<Long, LocalFlag> {
        // Export format structure varies, this is a basic parser
        // May need adjustment based on actual export format
        val flags = mutableMapOf<Long, LocalFlag>()
        
        try {
            json.forEach { (key, value) ->
                val flagId = key.toLongOrNull() ?: return@forEach
                val flagObj = value.jsonObject
                
                val segments = flagObj["segments"]?.jsonArray?.map { segmentEl ->
                    val segmentObj = segmentEl.jsonObject
                    LocalSegment(
                        id = segmentObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                        rank = segmentObj["rank"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        rolloutPercent = segmentObj["rolloutPercent"]?.jsonPrimitive?.content?.toIntOrNull() ?: 100,
                        constraints = segmentObj["constraints"]?.jsonArray?.map { constraintEl ->
                            val constraintObj = constraintEl.jsonObject
                            LocalConstraint(
                                id = constraintObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                                property = constraintObj["property"]?.jsonPrimitive?.content ?: "",
                                operator = constraintObj["operator"]?.jsonPrimitive?.content ?: "EQ",
                                value = constraintObj["value"]?.jsonPrimitive?.content
                            )
                        } ?: emptyList(),
                        distributions = segmentObj["distributions"]?.jsonArray?.map { distEl ->
                            val distObj = distEl.jsonObject
                            LocalDistribution(
                                id = distObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                                variantID = distObj["variantID"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                                variantKey = distObj["variantKey"]?.jsonPrimitive?.content ?: "",
                                percent = distObj["percent"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()
                
                val variants = flagObj["variants"]?.jsonArray?.map { variantEl ->
                    val variantObj = variantEl.jsonObject
                    LocalVariant(
                        id = variantObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                        key = variantObj["key"]?.jsonPrimitive?.content ?: "",
                        attachment = variantObj["attachment"]?.jsonObject
                    )
                } ?: emptyList()
                
                flags[flagId] = LocalFlag(
                    id = flagId,
                    key = flagObj["key"]?.jsonPrimitive?.content ?: "",
                    enabled = flagObj["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                    segments = segments,
                    variants = variants,
                    description = flagObj["description"]?.jsonPrimitive?.content,
                    entityType = flagObj["entityType"]?.jsonPrimitive?.content
                )
            }
        } catch (e: Exception) {
            // If parsing fails, return empty map (will trigger fallback)
            throw e
        }
        
        return flags
    }

    /**
     * Convert Map (from Export API response) to JsonObject for parsing.
     */
    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        return buildJsonObject {
            map.forEach { (k, v) ->
                put(k, anyToJsonElement(v))
            }
        }
    }

    private fun anyToJsonElement(v: Any?): JsonElement {
        if (v == null) return JsonPrimitive("")
        return when (v) {
            is Map<*, *> -> mapToJsonObject(v as Map<String, Any?>)
            is List<*> -> JsonArray(v.map { anyToJsonElement(it) })
            is Number -> JsonPrimitive(v)
            is Boolean -> JsonPrimitive(v)
            is String -> JsonPrimitive(v)
            else -> JsonPrimitive(v.toString())
        }
    }

    /**
     * Generate revision ID for snapshot.
     */
    private fun generateRevision(): String {
        return "snapshot-${System.currentTimeMillis()}"
    }

    /**
     * Fetch only flags that changed since last revision.
     *
     * @param lastRevision Last known revision
     * @param ttlMs TTL for the snapshot
     * @return Updated snapshot or null if no changes
     */
    suspend fun fetchDelta(lastRevision: String?, ttlMs: Long = 300000): FlagSnapshot? {
        // For now, always fetch full snapshot
        // TODO: Implement delta fetch when backend supports it
        return fetchSnapshot(ttlMs)
    }
}
