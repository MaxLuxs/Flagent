package com.flagent.enhanced.fetcher

import com.flagent.client.apis.ExportApi
import com.flagent.client.apis.FlagApi
import com.flagent.enhanced.model.FlagSnapshot
import com.flagent.enhanced.model.LocalConstraint
import com.flagent.enhanced.model.LocalDistribution
import com.flagent.enhanced.model.LocalFlag
import com.flagent.enhanced.model.LocalSegment
import com.flagent.enhanced.model.LocalVariant
import com.flagent.enhanced.platform.currentTimeMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * JVM implementation of [SnapshotFetcher] using kotlin-client ExportApi/FlagApi.
 */
@Suppress("UNCHECKED_CAST")
class DefaultSnapshotFetcher(
    private val exportApi: ExportApi,
    private val flagApi: FlagApi
) : SnapshotFetcher {

    override suspend fun fetchSnapshot(ttlMs: Long): FlagSnapshot = withContext(Dispatchers.IO) {
        try {
            val response = exportApi.getExportEvalCacheJSON()
            val body = response.body()
            val json = mapToJsonObject(body)
            val flags = parseExportJson(json)
            FlagSnapshot(
                flags = flags,
                revision = generateRevision(),
                fetchedAt = currentTimeMs(),
                ttlMs = ttlMs
            )
        } catch (_: Exception) {
            fetchSnapshotFallback(ttlMs)
        }
    }

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
        val flags = flagsList.associate { flagDto ->
            val flagId = flagDto.id
            val segments = flagDto.segments?.map { segmentDto ->
                LocalSegment(
                    id = segmentDto.id,
                    rank = segmentDto.rank.toInt(),
                    rolloutPercent = segmentDto.rolloutPercent.toInt(),
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
        }
        return FlagSnapshot(
            flags = flags,
            revision = generateRevision(),
            fetchedAt = currentTimeMs(),
            ttlMs = ttlMs
        )
    }

    private fun parseExportJson(json: JsonObject): Map<Long, LocalFlag> {
        val flags = mutableMapOf<Long, LocalFlag>()
        val flagsArray = json["flags"]?.jsonArray
        if (flagsArray != null) {
            for (flagEl in flagsArray) {
                val flagObj = flagEl.jsonObject
                val flagId = flagObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: continue
                flags[flagId] = parseFlagObject(flagObj, flagId)
            }
        } else {
            json.forEach { (key, value) ->
                val flagId = key.toLongOrNull() ?: return@forEach
                val flagObj = value.jsonObject
                flags[flagId] = parseFlagObject(flagObj, flagId)
            }
        }
        return flags
    }

    private fun parseFlagObject(flagObj: JsonObject, flagId: Long): LocalFlag {
        val segments = flagObj["segments"]?.jsonArray?.map { segmentEl ->
            val segmentObj = segmentEl.jsonObject
            LocalSegment(
                id = segmentObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                rank = segmentObj["rank"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                rolloutPercent = segmentObj["rolloutPercent"]?.jsonPrimitive?.content?.toIntOrNull()
                    ?: 100,
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
                        variantID = distObj["variantId"]?.jsonPrimitive?.content?.toLongOrNull()
                            ?: distObj["variantID"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                        variantKey = distObj["variantKey"]?.jsonPrimitive?.content ?: "",
                        percent = distObj["percent"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    )
                } ?: emptyList()
            )
        } ?: emptyList()
        val variants = flagObj["variants"]?.jsonArray?.map { variantEl ->
            val variantObj = variantEl.jsonObject
            val attachment = variantObj["attachment"] as? JsonObject
            LocalVariant(
                id = variantObj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                key = variantObj["key"]?.jsonPrimitive?.content ?: "",
                attachment = attachment
            )
        } ?: emptyList()
        return LocalFlag(
            id = flagId,
            key = flagObj["key"]?.jsonPrimitive?.content ?: "",
            enabled = flagObj["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
            segments = segments,
            variants = variants,
            description = flagObj["description"]?.jsonPrimitive?.content,
            entityType = flagObj["entityType"]?.jsonPrimitive?.content
        )
    }

    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject = buildJsonObject {
        map.forEach { (k, v) -> put(k, anyToJsonElement(v)) }
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

    private fun generateRevision(): String = "snapshot-${currentTimeMs()}"

    override suspend fun fetchDelta(lastRevision: String?, ttlMs: Long): FlagSnapshot? =
        fetchSnapshot(ttlMs)
}
