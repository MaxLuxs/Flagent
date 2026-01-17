package flagent.api.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/**
 * Tests for Evaluation API models serialization/deserialization
 */
class EvaluationModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testEvaluationRequestSerialization() {
        val request = EvaluationRequest(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "US"),
            flagID = 1,
            enableDebug = true
        )
        val jsonString = json.encodeToString(EvaluationRequest.serializer(), request)
        assertNotNull(jsonString)
        
        val deserialized = json.decodeFromString(EvaluationRequest.serializer(), jsonString)
        assertEquals("user123", deserialized.entityID)
        assertEquals("user", deserialized.entityType)
        assertEquals("US", deserialized.entityContext?.get("country"))
        assertEquals(1, deserialized.flagID)
        assertEquals(true, deserialized.enableDebug)
    }

    @Test
    fun testEvaluationRequestWithFlagKey() {
        val request = EvaluationRequest(
            flagKey = "test_flag",
            enableDebug = false
        )
        val jsonString = json.encodeToString(EvaluationRequest.serializer(), request)
        val deserialized = json.decodeFromString(EvaluationRequest.serializer(), jsonString)
        assertEquals("test_flag", deserialized.flagKey)
        assertEquals(false, deserialized.enableDebug)
    }

    @Test
    fun testEvaluationBatchRequestSerialization() {
        val entity = EntityRequest(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "US")
        )
        val request = EvaluationBatchRequest(
            entities = listOf(entity),
            flagIDs = listOf(1, 2),
            flagKeys = listOf("flag1", "flag2"),
            flagTags = listOf("production"),
            flagTagsOperator = "ANY",
            enableDebug = true
        )
        val jsonString = json.encodeToString(EvaluationBatchRequest.serializer(), request)
        val deserialized = json.decodeFromString(EvaluationBatchRequest.serializer(), jsonString)
        assertEquals(1, deserialized.entities.size)
        assertEquals(2, deserialized.flagIDs.size)
        assertEquals(2, deserialized.flagKeys.size)
        assertEquals(1, deserialized.flagTags.size)
        assertEquals("ANY", deserialized.flagTagsOperator)
        assertEquals(true, deserialized.enableDebug)
    }

    @Test
    fun testEntityRequestSerialization() {
        val entity = EntityRequest(
            entityID = "user456",
            entityType = "user",
            entityContext = mapOf("city" to "NYC")
        )
        val jsonString = json.encodeToString(EntityRequest.serializer(), entity)
        val deserialized = json.decodeFromString(EntityRequest.serializer(), jsonString)
        assertEquals("user456", deserialized.entityID)
        assertEquals("user", deserialized.entityType)
        assertEquals("NYC", deserialized.entityContext?.get("city"))
    }

    @Test
    fun testEvaluationResponseSerialization() {
        val evalContext = EvalContextResponse(
            entityID = "user123",
            entityType = "user",
            entityContext = mapOf("country" to "US")
        )
        val debugLog = EvalDebugLogResponse(
            msg = "Debug info",
            segmentDebugLogs = listOf(
                SegmentDebugLogResponse(segmentID = 1, msg = "Segment matched")
            )
        )
        val response = EvaluationResponse(
            flagID = 1,
            flagKey = "test_flag",
            flagSnapshotID = 1,
            flagTags = listOf("production"),
            segmentID = 1,
            variantID = 1,
            variantKey = "variant_a",
            variantAttachment = mapOf("color" to "red"),
            evalContext = evalContext,
            evalDebugLog = debugLog,
            timestamp = 1234567890L
        )
        val jsonString = json.encodeToString(EvaluationResponse.serializer(), response)
        val deserialized = json.decodeFromString(EvaluationResponse.serializer(), jsonString)
        assertEquals(1, deserialized.flagID)
        assertEquals("test_flag", deserialized.flagKey)
        assertEquals(1, deserialized.segmentID)
        assertEquals(1, deserialized.variantID)
        assertEquals("variant_a", deserialized.variantKey)
        assertEquals("red", deserialized.variantAttachment?.get("color"))
        assertEquals(1234567890L, deserialized.timestamp)
    }

    @Test
    fun testEvalContextResponseSerialization() {
        val context = EvalContextResponse(
            entityID = "user789",
            entityType = "user",
            entityContext = mapOf("lang" to "en")
        )
        val jsonString = json.encodeToString(EvalContextResponse.serializer(), context)
        val deserialized = json.decodeFromString(EvalContextResponse.serializer(), jsonString)
        assertEquals("user789", deserialized.entityID)
        assertEquals("user", deserialized.entityType)
        assertEquals("en", deserialized.entityContext?.get("lang"))
    }

    @Test
    fun testEvalDebugLogResponseSerialization() {
        val debugLog = EvalDebugLogResponse(
            msg = "Evaluation debug",
            segmentDebugLogs = listOf(
                SegmentDebugLogResponse(segmentID = 1, msg = "Segment 1 matched"),
                SegmentDebugLogResponse(segmentID = 2, msg = "Segment 2 skipped")
            )
        )
        val jsonString = json.encodeToString(EvalDebugLogResponse.serializer(), debugLog)
        val deserialized = json.decodeFromString(EvalDebugLogResponse.serializer(), jsonString)
        assertEquals("Evaluation debug", deserialized.msg)
        assertEquals(2, deserialized.segmentDebugLogs.size)
        assertEquals(1, deserialized.segmentDebugLogs.first().segmentID)
    }

    @Test
    fun testSegmentDebugLogResponseSerialization() {
        val log = SegmentDebugLogResponse(segmentID = 5, msg = "Segment matched")
        val jsonString = json.encodeToString(SegmentDebugLogResponse.serializer(), log)
        val deserialized = json.decodeFromString(SegmentDebugLogResponse.serializer(), jsonString)
        assertEquals(5, deserialized.segmentID)
        assertEquals("Segment matched", deserialized.msg)
    }

    @Test
    fun testEvaluationBatchResponseSerialization() {
        val evalContext = EvalContextResponse(entityID = "user123")
        val evalResponse1 = EvaluationResponse(
            flagID = 1,
            flagKey = "flag1",
            flagSnapshotID = 1,
            flagTags = emptyList(),
            evalContext = evalContext,
            timestamp = 1000L
        )
        val evalResponse2 = EvaluationResponse(
            flagID = 2,
            flagKey = "flag2",
            flagSnapshotID = 2,
            flagTags = emptyList(),
            evalContext = evalContext,
            timestamp = 2000L
        )
        val batchResponse = EvaluationBatchResponse(
            evaluationResults = listOf(evalResponse1, evalResponse2)
        )
        val jsonString = json.encodeToString(EvaluationBatchResponse.serializer(), batchResponse)
        val deserialized = json.decodeFromString(EvaluationBatchResponse.serializer(), jsonString)
        assertEquals(2, deserialized.evaluationResults.size)
        assertEquals("flag1", deserialized.evaluationResults.first().flagKey)
        assertEquals("flag2", deserialized.evaluationResults.last().flagKey)
    }
}
