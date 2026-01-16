package flagent.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger {}

/**
 * Documentation routes - Swagger UI and OpenAPI specification
 */
fun Routing.configureDocumentationRoutes() {
    // GET /docs - Swagger UI interface (top-level route)
    get("/docs") {
        val swaggerUiHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Flagent API Documentation</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui.css" />
    <style>
        :root {
            --swagger-primary: #0EA5E9;
            --swagger-primary-dark: #0284C7;
            --swagger-secondary: #14B8A6;
            --swagger-text: #0F172A;
            --swagger-bg: #ffffff;
            --swagger-border: #E2E8F0;
        }
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *, *:before, *:after {
            box-sizing: inherit;
        }
        body {
            margin:0;
            background: #F8FAFC;
        }
        .swagger-ui .topbar {
            background-color: var(--swagger-primary);
        }
        .swagger-ui .topbar .download-url-wrapper {
            display: none;
        }
        .swagger-ui .info .title {
            color: var(--swagger-text);
        }
        .swagger-ui .btn.authorize {
            background-color: var(--swagger-primary);
            border-color: var(--swagger-primary);
        }
        .swagger-ui .btn.authorize:hover {
            background-color: var(--swagger-primary-dark);
            border-color: var(--swagger-primary-dark);
        }
        .swagger-ui .opblock.opblock-post .opblock-summary {
            border-color: var(--swagger-primary);
        }
        .swagger-ui .opblock.opblock-post .opblock-summary-method {
            background: var(--swagger-primary);
        }
        .swagger-ui .opblock.opblock-get .opblock-summary {
            border-color: var(--swagger-secondary);
        }
        .swagger-ui .opblock.opblock-get .opblock-summary-method {
            background: var(--swagger-secondary);
        }
        .swagger-ui .btn.execute {
            background-color: var(--swagger-primary);
            border-color: var(--swagger-primary);
        }
        .swagger-ui .btn.execute:hover {
            background-color: var(--swagger-primary-dark);
            border-color: var(--swagger-primary-dark);
        }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-standalone-preset.js"></script>
    <script>
        window.onload = function() {
            const ui = SwaggerUIBundle({
                url: "/api/v1/openapi.yaml",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout",
                validatorUrl: null
            });
        };
    </script>
</body>
</html>
        """.trimIndent()
        
        call.respondText(
            swaggerUiHtml,
            ContentType.Text.Html,
            HttpStatusCode.OK
        )
    }
    
    // GET /api/v1/openapi.yaml - OpenAPI specification in YAML format
    route("/api/v1") {
            get("/openapi.yaml") {
                try {
                    // Load OpenAPI file from file system
                    // Try multiple possible locations
                    val content = run {
                        val currentDir = File(System.getProperty("user.dir"))
                        val possiblePaths = listOf(
                            File(currentDir, "docs/api/openapi.yaml"), // From project root
                            File(currentDir.parentFile, "docs/api/openapi.yaml"), // From backend directory
                            File(currentDir.parentFile?.parentFile, "docs/api/openapi.yaml") // From workspace root
                        )
                        
                        possiblePaths.firstOrNull { it.exists() }?.readText()
                            ?: throw FileNotFoundException(
                                "OpenAPI specification not found. Tried: ${possiblePaths.joinToString { it.absolutePath }}"
                            )
                    }
                    call.respondText(
                        content,
                        ContentType.parse("application/x-yaml"),
                        HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    logger.error(e) { "Failed to load OpenAPI specification" }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("message" to "Failed to load OpenAPI specification: ${e.message}")
                    )
                }
            }
            
            // GET /api/v1/openapi.json - OpenAPI specification in JSON format (optional)
            get("/openapi.json") {
                try {
                    // Load OpenAPI file from file system
                    // Try multiple possible locations
                    val yamlContent = run {
                        val currentDir = File(System.getProperty("user.dir"))
                        val possiblePaths = listOf(
                            File(currentDir, "docs/api/openapi.yaml"), // From project root
                            File(currentDir.parentFile, "docs/api/openapi.yaml"), // From backend directory
                            File(currentDir.parentFile?.parentFile, "docs/api/openapi.yaml") // From workspace root
                        )
                        
                        possiblePaths.firstOrNull { it.exists() }?.readText()
                            ?: throw FileNotFoundException(
                                "OpenAPI specification not found. Tried: ${possiblePaths.joinToString { it.absolutePath }}"
                            )
                    }
                    
                    // Convert YAML to JSON using Jackson
                    val yamlMapper = com.fasterxml.jackson.dataformat.yaml.YAMLMapper()
                    val jsonMapper = com.fasterxml.jackson.databind.ObjectMapper()
                    val jsonNode = yamlMapper.readTree(yamlContent)
                    val jsonContent = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
                    
                    call.respondText(
                        jsonContent,
                        ContentType.Application.Json,
                        HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    logger.error(e) { "Failed to convert OpenAPI specification to JSON" }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("message" to "Failed to convert OpenAPI specification: ${e.message}")
                    )
                }
            }
    }
}
