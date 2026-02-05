package flagent.route

import flagent.repository.impl.EvaluationEventRepository
import flagent.repository.impl.FlagEvaluationStatsResult
import flagent.repository.impl.MetricsOverviewResult
import flagent.repository.impl.TimeSeriesEntry
import flagent.repository.impl.TopFlagEntry
import flagent.service.CoreMetricsService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.*

class CoreMetricsRoutesTest {

    @Test
    fun getOverview_returns200WithJson() = testApplication {
        val overview = MetricsOverviewResult(
            totalEvaluations = 100L,
            uniqueFlags = 3,
            topFlags = listOf(
                TopFlagEntry(1, "flag_a", 50L),
                TopFlagEntry(2, "flag_b", 30L)
            ),
            timeSeries = listOf(
                TimeSeriesEntry(1_700_000_000_000L, 40L),
                TimeSeriesEntry(1_700_000_360_000L, 60L)
            )
        )
        val repo = mockk<EvaluationEventRepository>()
        coEvery { repo.getOverview(any(), any(), any(), any()) } returns overview
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("start", 1_700_000_000_000L)
            parameter("end", 1_700_001_000_000L)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<MetricsOverviewResult>(response.bodyAsText())
        assertEquals(100L, body.totalEvaluations)
        assertEquals(3, body.uniqueFlags)
        assertEquals(2, body.topFlags.size)
        assertEquals("flag_a", body.topFlags[0].flagKey)
        assertEquals(2, body.timeSeries.size)
    }

    @Test
    fun getOverview_missingStart_returns400() = testApplication {
        val repo = mockk<EvaluationEventRepository>()
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("end", 1_700_001_000_000L)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("start") || response.bodyAsText().contains("error"))
    }

    @Test
    fun getOverview_missingEnd_returns400() = testApplication {
        val repo = mockk<EvaluationEventRepository>()
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("start", 1_700_000_000_000L)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getOverview_usesDefaultTopLimitAndTimeBucket() = testApplication {
        val overview = MetricsOverviewResult(
            totalEvaluations = 0L,
            uniqueFlags = 0,
            topFlags = emptyList(),
            timeSeries = emptyList()
        )
        val repo = mockk<EvaluationEventRepository>()
        coEvery { repo.getOverview(any(), any(), 10, 3600_000L) } returns overview
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("start", 1_700_000_000_000L)
            parameter("end", 1_700_001_000_000L)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getOverview_invalidStart_returns400() = testApplication {
        val repo = mockk<EvaluationEventRepository>()
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("start", "invalid")
            parameter("end", 1_700_001_000_000L)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getOverview_invalidEnd_returns400() = testApplication {
        val repo = mockk<EvaluationEventRepository>()
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service)
            }
        }

        val response = client.get("/api/v1/metrics/overview") {
            parameter("start", 1_700_000_000_000L)
            parameter("end", "not-a-number")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getFlagEvaluationStats_returns200WithJson() = testApplication {
        val stats = FlagEvaluationStatsResult(
            flagId = 1,
            evaluationCount = 42L,
            timeSeries = listOf(
                TimeSeriesEntry(1_700_000_000_000L, 20L),
                TimeSeriesEntry(1_700_000_360_000L, 22L)
            )
        )
        val repo = mockk<EvaluationEventRepository>()
        coEvery { repo.getStatsForFlag(1, any(), any(), any()) } returns stats
        val service = CoreMetricsService(repo)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                configureCoreMetricsRoutes(service, null)
            }
        }

        val response = client.get("/api/v1/flags/1/evaluation-stats") {
            parameter("start", 1_700_000_000_000L)
            parameter("end", 1_700_001_000_000L)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<FlagEvaluationStatsResult>(response.bodyAsText())
        assertEquals(1, body.flagId)
        assertEquals(42L, body.evaluationCount)
        assertEquals(2, body.timeSeries.size)
    }
}
