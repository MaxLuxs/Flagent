package flagent.route

import flagent.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.io.File
import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.*
import com.sun.management.HotSpotDiagnosticMXBean

private val logger = KotlinLogging.logger {}

/**
 * Profiling routes - JVM profiling endpoints (heap/thread dumps, CPU profiling):
 * - /debug/pprof/heap - heap dump
 * - /debug/pprof/thread - thread dump
 * - /debug/pprof/profile - CPU profile (via JFR or JMX)
 */
fun Routing.configureProfilingRoutes() {
    if (!AppConfig.pprofEnabled) {
        return
    }
    
    route("/debug/pprof") {
        // Heap dump endpoint
        get("/heap") {
            try {
                val tempFile = File.createTempFile("heap_dump_", ".hprof")
                tempFile.deleteOnExit()
                try {
                    val server = ManagementFactory.getPlatformMBeanServer()
                    val proxy = ManagementFactory.newPlatformMXBeanProxy(
                        server,
                        "com.sun.management:type=HotSpotDiagnostic",
                        HotSpotDiagnosticMXBean::class.java
                    )
                    
                    proxy.dumpHeap(tempFile.absolutePath, false)
                    
                    val heapDumpBytes = tempFile.readBytes()
                    call.response.headers.append("Content-Disposition", "attachment; filename=heap_dump.hprof")
                    call.respondBytes(
                        bytes = heapDumpBytes,
                        contentType = ContentType.Application.OctetStream
                    )
                } finally {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate heap dump" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to generate heap dump: ${e.message}")
                )
            }
        }
        
        // Thread dump endpoint
        get("/thread") {
            try {
                val threadMXBean = ManagementFactory.getThreadMXBean()
                val threadInfos = threadMXBean.dumpAllThreads(true, true)
                
                val threadDump = buildString {
                    appendLine("Thread Dump - ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")
                    appendLine("=".repeat(80))
                    threadInfos.forEach { threadInfo ->
                        appendLine("\"${threadInfo.threadName}\"")
                        appendLine("   java.lang.Thread.State: ${threadInfo.threadState}")
                        if (threadInfo.lockedMonitors.isNotEmpty()) {
                            appendLine("   Locked monitors:")
                            threadInfo.lockedMonitors.forEach { monitor ->
                                appendLine("      - ${monitor.lockedStackFrame}")
                            }
                        }
                        if (threadInfo.lockedSynchronizers.isNotEmpty()) {
                            appendLine("   Locked synchronizers:")
                            threadInfo.lockedSynchronizers.forEach { sync ->
                                appendLine("      - $sync")
                            }
                        }
                        appendLine()
                        threadInfo.stackTrace.forEach { element ->
                            appendLine("      at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                        }
                        appendLine()
                    }
                }
                
                call.response.headers.append("Content-Type", "text/plain")
                call.respondText(threadDump, ContentType.Text.Plain)
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate thread dump" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to generate thread dump: ${e.message}")
                )
            }
        }
        
        // CPU profile endpoint (simplified - returns thread dump as CPU profile analog)
        get("/profile") {
            try {
                // CPU profiling in JVM typically requires JFR (Java Flight Recorder) or external tools
                // For simplicity, we return thread dump as CPU profile analog
                // Full CPU profiling would require JFR integration or external tools
                val threadMXBean = ManagementFactory.getThreadMXBean()
                val threadInfos = threadMXBean.dumpAllThreads(true, true)
                
                val profile = buildString {
                    appendLine("CPU Profile (Thread Dump) - ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")
                    appendLine("=".repeat(80))
                    appendLine("Note: Full CPU profiling requires Java Flight Recorder (JFR) or external tools")
                    appendLine()
                    threadInfos.forEach { threadInfo ->
                        appendLine("\"${threadInfo.threadName}\"")
                        appendLine("   State: ${threadInfo.threadState}")
                        threadInfo.stackTrace.forEach { element ->
                            appendLine("      ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                        }
                        appendLine()
                    }
                }
                
                call.response.headers.append("Content-Type", "text/plain")
                call.respondText(profile, ContentType.Text.Plain)
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate CPU profile" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to generate CPU profile: ${e.message}")
                )
            }
        }
    }
}
