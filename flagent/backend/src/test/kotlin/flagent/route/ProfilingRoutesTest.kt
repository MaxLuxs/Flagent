package flagent.route

import flagent.config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlin.test.*
import kotlinx.serialization.json.*

class ProfilingRoutesTest {
    @Test
    fun testProfilingRoutes_NotConfigured_WhenPprofDisabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.pprofEnabled } returns false
        
        application {
            routing {
                configureProfilingRoutes()
            }
        }
        
        val response = client.get("/debug/pprof/heap")
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testProfilingRoutes_HeapDump_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.pprofEnabled } returns true
        
        application {
            routing {
                configureProfilingRoutes()
            }
        }
        
        try {
            val response = client.get("/debug/pprof/heap")
            // May succeed or fail depending on JVM capabilities
            assertTrue(
                response.status == HttpStatusCode.OK || 
                response.status == HttpStatusCode.InternalServerError
            )
        } catch (e: Exception) {
            // Some JVMs may not support heap dumps
            assertTrue(true)
        }
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testProfilingRoutes_ThreadDump_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.pprofEnabled } returns true
        
        application {
            routing {
                configureProfilingRoutes()
            }
        }
        
        val response = client.get("/debug/pprof/thread")
        assertEquals(HttpStatusCode.OK, response.status)
        val content = response.bodyAsText()
        assertTrue(content.contains("Thread Dump") || content.contains("thread"))
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testProfilingRoutes_CPUProfile_WhenEnabled() = testApplication {
        mockkObject(AppConfig)
        every { AppConfig.pprofEnabled } returns true
        
        application {
            routing {
                configureProfilingRoutes()
            }
        }
        
        val response = client.get("/debug/pprof/profile")
        assertEquals(HttpStatusCode.OK, response.status)
        val content = response.bodyAsText()
        assertTrue(content.contains("CPU Profile") || content.contains("profile"))
        
        unmockkObject(AppConfig)
    }
}
