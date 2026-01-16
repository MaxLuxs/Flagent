package flagent.route

import flagent.api.model.*
import flagent.service.FlagSnapshotService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

/**
 * FlagSnapshot routes - operations for flag snapshots
 * Maps to pkg/handler/crud.go from original project
 */
fun Routing.configureFlagSnapshotRoutes(flagSnapshotService: FlagSnapshotService) {
    route("/api/v1") {
            route("/flags/{flagId}/snapshots") {
                get {
                    val flagId = call.parameters["flagId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid flag ID")
                    
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val sort = call.request.queryParameters["sort"]
                    
                    try {
                        val snapshots = flagSnapshotService.findSnapshotsByFlagId(flagId, limit, offset, sort)
                        val response = snapshots.map { mapFlagSnapshotToResponse(it) }
                        call.respond(response)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound, e.message ?: "Flag not found")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                }
            }
        }
}

private fun mapFlagSnapshotToResponse(snapshot: flagent.domain.entity.FlagSnapshot): FlagSnapshotResponse {
    // Parse flag JSON
    val json = Json { ignoreUnknownKeys = true }
    val flagJson = try {
        json.parseToJsonElement(snapshot.flag) as? kotlinx.serialization.json.JsonObject
    } catch (e: Exception) {
        null
    }
    
    // Map flag from JSON
    val flagResponse = flagJson?.let { jsonObj ->
        try {
            val flagId = jsonObj["id"]?.toString()?.toIntOrNull() ?: 0
            val key = jsonObj["key"]?.toString() ?: ""
            val description = jsonObj["description"]?.toString() ?: ""
            val enabled = jsonObj["enabled"]?.toString()?.toBoolean() ?: false
            val snapshotId = jsonObj["snapshotId"]?.toString()?.toIntOrNull() ?: 0
            val dataRecordsEnabled = jsonObj["dataRecordsEnabled"]?.toString()?.toBoolean() ?: false
            val entityType = jsonObj["entityType"]?.toString()
            val notes = jsonObj["notes"]?.toString()
            val createdBy = jsonObj["createdBy"]?.toString()
            val updatedBy = jsonObj["updatedBy"]?.toString()
            
            FlagResponse(
                id = flagId,
                key = key,
                description = description,
                createdBy = createdBy,
                updatedBy = updatedBy,
                enabled = enabled,
                snapshotID = snapshotId,
                notes = notes,
                dataRecordsEnabled = dataRecordsEnabled,
                entityType = entityType
            )
        } catch (e: Exception) {
            // Fallback to empty flag
            FlagResponse(
                id = 0,
                key = "",
                description = "",
                enabled = false,
                snapshotID = 0,
                dataRecordsEnabled = false
            )
        }
    } ?: FlagResponse(
        id = 0,
        key = "",
        description = "",
        enabled = false,
        snapshotID = 0,
        dataRecordsEnabled = false
    )
    
    return FlagSnapshotResponse(
        id = snapshot.id,
        updatedBy = snapshot.updatedBy,
        flag = flagResponse,
        updatedAt = snapshot.updatedAt ?: java.time.Instant.now().toString() // Fallback for backward compatibility
    )
}
