package flagent.mcp

import flagent.cache.impl.EvalCache
import flagent.service.EvaluationService
import flagent.service.FlagService
import flagent.service.command.CreateFlagCommand
import flagent.service.command.PutFlagCommand
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
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
import kotlinx.serialization.json.buildJsonArray
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val json = Json { ignoreUnknownKeys = true }

private fun errorResult(message: String): CallToolResult =
    CallToolResult(
        content = listOf(TextContent(text = json.encodeToString(mapOf("error" to message)))),
        isError = true
    )

/**
 * Creates and configures the Flagent MCP server with tools and resources.
 * When [flagService] is null (e.g. eval-only mode), create/update tools are not registered.
 */
fun createFlagentMcpServer(
    evaluationService: EvaluationService,
    evalCache: EvalCache,
    flagService: FlagService? = null
): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "flagent",
            version = "0.1.6"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(listChanged = true)
            )
        )
    )

    // --- evaluate_flag ---
    server.addTool(
        name = "evaluate_flag",
        description = "Evaluate a feature flag by key for a given entity. Returns assigned variant, attachment, and debug message. Use this to check what value a user/entity would get for a flag (e.g. for A/B tests or targeting).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key to evaluate (e.g. new_checkout_flow)"))
                })
                put("entityId", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Optional entity ID for targeting (user id, device id)"))
                })
                put("entityContext", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Optional JSON object string for targeting (e.g. {\"region\":\"EU\", \"tier\":\"premium\"})"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: evaluate_flag" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool errorResult("flagKey is required")
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
            try {
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
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] evaluate_flag failed" }
                errorResult(e.message ?: "Evaluation failed")
            }
        }
    }

    // --- list_flags ---
    server.addTool(
        name = "list_flags",
        description = "List enabled feature flags with optional filters. Returns id, key, description, enabled. Use tags filter for comma-separated tag values; use limit to cap results (default 50, max recommended 200).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("number"))
                    put("description", JsonPrimitive("Max number of flags to return (default 50)"))
                })
                put("tags", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Comma-separated tag values to filter (e.g. team:payments,env:prod)"))
                })
            }
        )
    ) { request ->
        logger.info { "[MCP] Tool called: list_flags" }
        runBlocking {
            try {
                val export = evalCache.export()
                val args = request.params.arguments ?: buildJsonObject { }
                val limit = (args["limit"]?.jsonPrimitive?.content?.toIntOrNull() ?: 50).coerceIn(1, 500)
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
                val arr = buildJsonArray { result.forEach { add(it) } }
                CallToolResult(content = listOf(TextContent(text = json.encodeToString(arr))))
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] list_flags failed" }
                errorResult(e.message ?: "List flags failed")
            }
        }
    }

    // --- get_flag ---
    server.addTool(
        name = "get_flag",
        description = "Get full flag configuration by key: segments, variants, distributions, constraints, tags. Use this to inspect how a flag is set up before evaluating or updating it.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key (e.g. new_checkout_flow)"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: get_flag" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool errorResult("flagKey is required")
        }
        runBlocking {
            try {
                val export = evalCache.export()
                val flag = export.flags.find { it.key == flagKey }
                if (flag == null) {
                    return@runBlocking CallToolResult(
                        content = listOf(TextContent(text = "{\"error\":\"Flag not found: $flagKey\"}")),
                        isError = true
                    )
                }
                CallToolResult(content = listOf(TextContent(text = json.encodeToString(flag))))
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] get_flag failed" }
                errorResult(e.message ?: "Get flag failed")
            }
        }
    }

    // --- create_flag (only when flagService is available) ---
    if (flagService != null) {
        server.addTool(
            name = "create_flag",
            description = "Create a new feature flag. Optionally use template 'simple_boolean_flag' to get one segment with variant 'on'. Returns created flag id and key. Use update_flag to set description or enable it.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("key", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Optional flag key (e.g. my_new_feature). If omitted, a random key is generated"))
                    })
                    put("description", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Short description of the flag (required)"))
                    })
                    put("template", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Optional template: simple_boolean_flag for on/off flag"))
                    })
                },
                required = listOf("description")
            )
        ) { request ->
            logger.info { "[MCP] Tool called: create_flag" }
            val args = request.params.arguments ?: buildJsonObject { }
            val key = args["key"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            val description = args["description"]?.jsonPrimitive?.content
            if (description.isNullOrBlank()) {
                return@addTool errorResult("description is required")
            }
            val template = args["template"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            runBlocking {
                try {
                    val created = flagService.createFlag(
                        CreateFlagCommand(key = key, description = description, template = template),
                        updatedBy = "mcp"
                    )
                    val resultJson = json.encodeToString(
                        mapOf(
                            "id" to created.id,
                            "key" to created.key,
                            "description" to created.description,
                            "enabled" to created.enabled
                        )
                    )
                    CallToolResult(content = listOf(TextContent(text = resultJson)))
                } catch (e: Exception) {
                    logger.warn(e) { "[MCP] create_flag failed" }
                    errorResult(e.message ?: "Create flag failed")
                }
            }
        }

        server.addTool(
            name = "update_flag",
            description = "Update an existing feature flag by key. You can change description, notes, entityType, dataRecordsEnabled. Use get_flag first to see current state.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("flagKey", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Flag key to update"))
                    })
                    put("description", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("New description"))
                    })
                    put("notes", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Internal notes"))
                    })
                    put("entityType", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Entity type for targeting (e.g. user, device)"))
                    })
                    put("dataRecordsEnabled", buildJsonObject {
                        put("type", JsonPrimitive("boolean"))
                        put("description", JsonPrimitive("Enable analytics recording for this flag"))
                    })
                    put("enabled", buildJsonObject {
                        put("type", JsonPrimitive("boolean"))
                        put("description", JsonPrimitive("Turn flag on or off (enable/disable)"))
                    })
                },
                required = listOf("flagKey")
            )
        ) { request ->
            logger.info { "[MCP] Tool called: update_flag" }
            val args = request.params.arguments ?: buildJsonObject { }
            val flagKey = args["flagKey"]?.jsonPrimitive?.content
            if (flagKey.isNullOrBlank()) {
                return@addTool errorResult("flagKey is required")
            }
            val description = args["description"]?.jsonPrimitive?.content
            val notes = args["notes"]?.jsonPrimitive?.content
            val entityType = args["entityType"]?.jsonPrimitive?.content
            val dataRecordsEnabled = args["dataRecordsEnabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
            val enabled = args["enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
            runBlocking {
                try {
                    val existing = flagService.findFlags(limit = 1, key = flagKey).firstOrNull()
                        ?: return@runBlocking errorResult("Flag not found: $flagKey")
                    var updated = flagService.updateFlag(
                        existing.id,
                        PutFlagCommand(
                            description = description,
                            key = flagKey,
                            dataRecordsEnabled = dataRecordsEnabled,
                            entityType = entityType,
                            notes = notes
                        ),
                        updatedBy = "mcp"
                    )
                    if (enabled != null) {
                        updated = flagService.setFlagEnabled(existing.id, enabled, "mcp") ?: updated
                    }
                    val resultJson = json.encodeToString(
                        mapOf(
                            "id" to updated.id,
                            "key" to updated.key,
                            "description" to updated.description,
                            "enabled" to updated.enabled
                        )
                    )
                    CallToolResult(content = listOf(TextContent(text = resultJson)))
                } catch (e: Exception) {
                    logger.warn(e) { "[MCP] update_flag failed" }
                    errorResult(e.message ?: "Update flag failed")
                }
            }
        }
    }

    // --- analyze_flags ---
    server.addTool(
        name = "analyze_flags",
        description = "Get a summary of all enabled flags: total count, list of keys and descriptions. Useful for AI to understand what flags exist before suggesting changes or answering questions.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("limit", buildJsonObject {
                    put("type", JsonPrimitive("number"))
                    put("description", JsonPrimitive("Max flags to include in list (default 100)"))
                })
            }
        )
    ) { request ->
        logger.info { "[MCP] Tool called: analyze_flags" }
        runBlocking {
            try {
                val export = evalCache.export()
                val args = request.params.arguments ?: buildJsonObject { }
                val limit = (args["limit"]?.jsonPrimitive?.content?.toIntOrNull() ?: 100).coerceIn(1, 500)
                val flags = export.flags.take(limit)
                val summary = buildJsonObject {
                    put("totalEnabled", JsonPrimitive(export.flags.size))
                    put("flags", buildJsonArray {
                        flags.forEach { f ->
                            add(buildJsonObject {
                                put("id", JsonPrimitive(f.id))
                                put("key", JsonPrimitive(f.key))
                                put("description", JsonPrimitive(f.description))
                                put("enabled", JsonPrimitive(f.enabled))
                                put("segmentsCount", JsonPrimitive(f.segments.size))
                                put("variantsCount", JsonPrimitive(f.variants.size))
                            })
                        }
                    })
                }
                CallToolResult(content = listOf(TextContent(text = json.encodeToString(summary))))
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] analyze_flags failed" }
                errorResult(e.message ?: "Analyze flags failed")
            }
        }
    }

    // --- suggest_segments ---
    server.addTool(
        name = "suggest_segments",
        description = "List segments for a flag by key: constraints and rollout percent. Use this to understand targeting rules and suggest new segments or changes.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key to get segments for"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: suggest_segments" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool errorResult("flagKey is required")
        }
        runBlocking {
            try {
                val export = evalCache.export()
                val flag = export.flags.find { it.key == flagKey }
                if (flag == null) {
                    return@runBlocking CallToolResult(
                        content = listOf(TextContent(text = "{\"error\":\"Flag not found: $flagKey\"}")),
                        isError = true
                    )
                }
                val segmentsSummary = flag.segments.map { s ->
                    buildJsonObject {
                        put("id", JsonPrimitive(s.id))
                        put("description", JsonPrimitive(s.description ?: ""))
                        put("rolloutPercent", JsonPrimitive(s.rolloutPercent))
                        put("constraintsCount", JsonPrimitive(s.constraints.size))
                        put("distributions", buildJsonArray {
                            s.distributions.forEach { d ->
                                add(buildJsonObject {
                                    put("variantKey", JsonPrimitive(d.variantKey ?: ""))
                                    put("percent", JsonPrimitive(d.percent))
                                })
                            }
                        })
                    }
                }
                val result = buildJsonObject {
                    put("flagKey", JsonPrimitive(flagKey))
                    put("segments", buildJsonArray { segmentsSummary.forEach { add(it) } })
                }
                CallToolResult(content = listOf(TextContent(text = json.encodeToString(result))))
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] suggest_segments failed" }
                errorResult(e.message ?: "Suggest segments failed")
            }
        }
    }

    // --- optimize_experiment ---
    server.addTool(
        name = "optimize_experiment",
        description = "Get variant distribution and rollout summary for a flag. Use this to analyze A/B experiment setup and suggest balance or rollout changes.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("flagKey", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("Flag key (experiment flag)"))
                })
            },
            required = listOf("flagKey")
        )
    ) { request ->
        logger.info { "[MCP] Tool called: optimize_experiment" }
        val args = request.params.arguments ?: buildJsonObject { }
        val flagKey = args["flagKey"]?.jsonPrimitive?.content
        if (flagKey.isNullOrBlank()) {
            return@addTool errorResult("flagKey is required")
        }
        runBlocking {
            try {
                val export = evalCache.export()
                val flag = export.flags.find { it.key == flagKey }
                if (flag == null) {
                    return@runBlocking CallToolResult(
                        content = listOf(TextContent(text = "{\"error\":\"Flag not found: $flagKey\"}")),
                        isError = true
                    )
                }
                val variants = flag.variants.map { v ->
                    buildJsonObject {
                        put("key", JsonPrimitive(v.key))
                        put("id", JsonPrimitive(v.id))
                    }
                }
                val segmentRollouts = flag.segments.map { s ->
                    buildJsonObject {
                        put("segmentId", JsonPrimitive(s.id))
                        put("rolloutPercent", JsonPrimitive(s.rolloutPercent))
                        put("distribution", buildJsonArray {
                            s.distributions.forEach { d ->
                                add(buildJsonObject {
                                    put("variantKey", JsonPrimitive(d.variantKey ?: ""))
                                    put("percent", JsonPrimitive(d.percent))
                                })
                            }
                        })
                    }
                }
                val result = buildJsonObject {
                    put("flagKey", JsonPrimitive(flagKey))
                    put("variants", buildJsonArray { variants.forEach { add(it) } })
                    put("segments", buildJsonArray { segmentRollouts.forEach { add(it) } })
                }
                CallToolResult(content = listOf(TextContent(text = json.encodeToString(result))))
            } catch (e: Exception) {
                logger.warn(e) { "[MCP] optimize_experiment failed" }
                errorResult(e.message ?: "Optimize experiment failed")
            }
        }
    }

    // --- Resources ---
    server.addResource(
        uri = "flagent://flags",
        name = "Enabled Flags",
        description = "JSON list of all enabled flags (id, key, description, segments, variants, tags)",
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
        description = "Full eval cache snapshot as JSON (all flags with full config)",
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
 * Pass [flagService] when not in eval-only mode to enable create_flag and update_flag tools.
 */
fun Routing.configureMcpRoutes(
    mcpPath: String,
    evaluationService: EvaluationService,
    evalCache: EvalCache,
    flagService: FlagService? = null
) {
    mcp(mcpPath) { createFlagentMcpServer(evaluationService, evalCache, flagService) }
    logger.info { "MCP server mounted at $mcpPath (write tools: ${flagService != null})" }
}
