package flagent.route

import flagent.api.constants.ApiConstants

import flagent.cache.impl.EvalCache
import flagent.cache.impl.EvalCacheJSON
import flagent.service.ExportService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Export API endpoints
 */
fun Routing.configureExportRoutes(evalCache: EvalCache, exportService: ExportService) {
    route(ApiConstants.API_BASE_PATH) {
            route("/export") {
                // GET /api/v1/export/gitops - export GitOps format (YAML/JSON) for import round-trip
                route("/gitops") {
                    get {
                        try {
                            val format = call.request.queryParameters["format"]?.lowercase() ?: "json"
                            val content = exportService.exportGitOps(format)
                            val contentType = when (format) {
                                "yaml", "yml" -> ContentType.parse("text/yaml; charset=utf-8")
                                else -> ContentType.Application.Json
                            }
                            call.respondText(content, contentType, HttpStatusCode.OK)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                }
                // GET /api/v1/export/eval_cache/json - export eval cache as JSON
                route("/eval_cache/json") {
                    get {
                        try {
                            val cacheJson = evalCache.export()
                            val json = Json { 
                                ignoreUnknownKeys = true
                                encodeDefaults = true
                            }
                            val jsonString = json.encodeToString(EvalCacheJSON.serializer(), cacheJson)
                            call.respondText(
                                jsonString,
                                ContentType.Application.Json,
                                HttpStatusCode.OK
                            )
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                }
                
                // GET /api/v1/export/sqlite - export database as SQLite file
                route("/sqlite") {
                    get {
                        try {
                            val excludeSnapshots = call.request.queryParameters["exclude_snapshots"]?.toBoolean() ?: false
                            val fileBytes = exportService.exportSQLite(excludeSnapshots)
                            
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                "attachment; filename=\"flagent_export.sqlite\""
                            )
                            call.respondBytes(
                                fileBytes,
                                ContentType.Application.OctetStream,
                                HttpStatusCode.OK
                            )
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                        }
                    }
                }
            }
        }
}
