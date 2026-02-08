package flagent.mcp

import flagent.cache.impl.EvalCache
import flagent.service.EvaluationService
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import io.ktor.server.routing.Routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonPrimitive
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val json = Json { ignoreUnknownKeys = true }

/**
 * Creates and configures the Flagent MCP server with tools and resources.
 */
fun createFlagentMcpServer(
    evaluationService: EvaluationService,
    evalCache: EvalCache
): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "flagent",
            version = "0.1.5"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(listChanged = true)
            )
        )
    )

    // Tools
    server.addTool(
        name = "evaluate_flag",
        description = "Evaluate a feature flag by key. Returns variant, attachment, and debug info.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key to evaluate"))
                })
                put("entityId", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Optional entity ID for targeting"))
                })
                put("entityContext", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Optional JSON string of entity context"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: evaluate_flag" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool CallToolResult(
                content = listOf(TextContent(text = "{\"error\":\"flagKey is required\"}")),
                isError = true
            )
        }
        val entityId = args["entityId"]?.jsonPrimitive?.content
        val entityContextStr = args["entityContext"]?.jsonPrimitive?.content
        val entityContext = entityContextStr?.let { str ->
            try {
                json.parseToJsonElement(str).jsonObject.entries.associate { (k, v) ->
                    k to when (v) {
                        is kotlinx.serialization.json.JsonPrimitive -> v.content
                        else -> v.toString()
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
        runBlocking {
            val result = evaluationService.evaluateFlag(
                flagID = null,
                flagKey = flagKey,
                entityID = entityId,
                entityType = null,
                entityContext = entityContext,
                enableDebug = true,
                environmentId = null
            )
            val resultJson = json.encodeToString(
                mapOf(
                    "flagId" to result.flagID,
                    "flagKey" to result.flagKey,
                    "variantId" to result.variantID,
                    "variantKey" to result.variantKey,
                    "variantAttachment" to (result.variantAttachment?.toString() ?: "null"),
                    "message" to (result.evalDebugLog?.message ?: "")
                )
            )
            CallToolResult(content = listOf(TextContent(text = resultJson)))
        }
    }

    server.addTool(
        name = "list_flags",
        description = "List enabled feature flags. Optionally filter by tags.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("number"))
                    put("description", JsonPrimitive("Max number of flags to return"))
                })
                put("tags", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Comma-separated tag values to filter"))
                })
            }
        )
    ) { request ->
        logger.info { "[MCP] Tool called: list_flags" }
        runBlocking {
            val export = evalCache.export()
            val args = request.params.arguments ?: buildJsonObject { }
            val limit = args["limit"]?.jsonPrimitive?.content?.toIntOrNull() ?: 50
            val tagsStr = args["tags"]?.jsonPrimitive?.content
            var flags = export.flags
            if (!tagsStr.isNullOrBlank()) {
                val tags = tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                flags = flags.filter { flag -> flag.tags.any { t -> t.value in tags } }
            }
            flags = flags.take(limit)
            val result = flags.map { f ->
                buildJsonObject {
                    put("id", JsonPrimitive(f.id))
                    put("key", JsonPrimitive(f.key))
                    put("description", JsonPrimitive(f.description))
                    put("enabled", JsonPrimitive(f.enabled))
                }
            }
            val arr = kotlinx.serialization.json.buildJsonArray { result.forEach { add(it) } }
            CallToolResult(content = listOf(TextContent(text = json.encodeToString(arr))))
        }
    }

    server.addTool(
        name = "get_flag",
        description = "Get full flag configuration by key.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: get_flag" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool CallToolResult(
                content = listOf(TextContent(text = "{\"error\":\"flagKey is required\"}")),
                isError = true
            )
        }
        runBlocking {
            val export = evalCache.export()
            val flag = export.flags.find { it.key == flagKey }
            if (flag == null) {
                return@runBlocking CallToolResult(
                    content = listOf(TextContent(text = "{\"error\":\"Flag not found: $flagKey\"}")),
                    isError = true
                )
            }
            val result = json.encodeToString(flag)
            CallToolResult(content = listOf(TextContent(text = result)))
        }
    }

    // Resources
    server.addResource(
        uri = "flagent://flags",
        name = "Enabled Flags",
        description = "JSON list of all enabled flags",
        mimeType = "application/json"
    ) { _ ->
        runBlocking {
            val export = evalCache.export()
            val jsonStr = json.encodeToString(export.flags)
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = "flagent://flags",
                        mimeType = "application/json",
                        text = jsonStr
                    )
                )
            )
        }
    }

    server.addResource(
        uri = "flagent://config/snapshot",
        name = "Eval Cache Snapshot",
        description = "Full eval cache snapshot as JSON",
        mimeType = "application/json"
    ) { _ ->
        runBlocking {
            val export = evalCache.export()
            val jsonStr = json.encodeToString(export)
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        uri = "flagent://config/snapshot",
                        mimeType = "application/json",
                        text = jsonStr
                    )
                )
            )
        }
    }

    return server
}

/**
 * Mounts the Flagent MCP server on the given routing at the specified path.
 */
fun Routing.configureMcpRoutes(
    mcpPath: String,
    evaluationService: EvaluationService,
    evalCache: EvalCache
) {
    mcp(mcpPath) { createFlagentMcpServer(evaluationService, evalCache) }
    logger.info { "MCP server mounted at $mcpPath" }
}
