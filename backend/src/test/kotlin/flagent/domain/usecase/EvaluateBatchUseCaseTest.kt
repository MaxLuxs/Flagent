package flagent.domain.usecase

import flagent.domain.entity.*
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import io.mockk.*
import kotlin.test.*

class EvaluateBatchUseCaseTest {
    private lateinit var evaluateFlagUseCase: EvaluateFlagUseCase
    private lateinit var evaluateBatchUseCase: EvaluateBatchUseCase
    
    @BeforeTest
    fun setup() {
        evaluateFlagUseCase = mockk()
        evaluateBatchUseCase = EvaluateBatchUseCase(evaluateFlagUseCase)
    }
    
    @Test
    fun testInvoke_ReturnsResults_WhenMultipleFlagsProvided() {
        val variant1 = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val variant2 = Variant(
            id = 2,
            flagId = 2,
            key = "variant2"
        )
        
        val distribution1 = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val distribution2 = Distribution(
            id = 2,
            segmentId = 2,
            variantId = 2,
            percent = 100
        )
        
        val segment1 = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution1)
        )
        
        val segment2 = Segment(
            id = 2,
            flagId = 2,
            description = "Segment 2",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution2)
        )
        
        val flag1 = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            segments = listOf(segment1),
            variants = listOf(variant1)
        )
        
        val flag2 = Flag(
            id = 2,
            key = "flag2",
            description = "Flag 2",
            enabled = true,
            segments = listOf(segment2),
            variants = listOf(variant2)
        )
        
        val result1 = EvaluateFlagUseCase.EvaluationResult(
            variantID = 1,
            segmentID = 1
        )
        
        val result2 = EvaluateFlagUseCase.EvaluationResult(
            variantID = 2,
            segmentID = 2
        )
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag1,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                enableDebug = false
            )
        } returns result1
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag2,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                enableDebug = false
            )
        } returns result2
        
        val results = evaluateBatchUseCase.invoke(
            flags = listOf(flag1, flag2),
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(2, results.size)
        assertEquals(1, results[0].flagID)
        assertEquals("flag1", results[0].flagKey)
        assertEquals(1, results[0].result.variantID)
        assertEquals(2, results[1].flagID)
        assertEquals("flag2", results[1].flagKey)
        assertEquals(2, results[1].result.variantID)
        
        verify {
            evaluateFlagUseCase.invoke(
                flag1,
                match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                false
            )
            evaluateFlagUseCase.invoke(
                flag2,
                match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                false
            )
        }
    }
    
    @Test
    fun testInvoke_ReturnsEmptyList_WhenNoFlagsProvided() {
        val results = evaluateBatchUseCase.invoke(
            flags = emptyList(),
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertTrue(results.isEmpty())
        verify(exactly = 0) { evaluateFlagUseCase.invoke(any(), any(), any()) }
    }
    
    @Test
    fun testInvoke_HandlesDisabledFlags() {
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = false
        )
        
        val result = EvaluateFlagUseCase.EvaluationResult(
            variantID = null,
            segmentID = null
        )
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                enableDebug = false
            )
        } returns result
        
        val results = evaluateBatchUseCase.invoke(
            flags = listOf(flag),
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, results.size)
        assertEquals(1, results[0].flagID)
        assertNull(results[0].result.variantID)
        
        verify {
            evaluateFlagUseCase.invoke(
                flag,
                match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                false
            )
        }
    }
    
    @Test
    fun testInvoke_HandlesFlagsWithNoSegments() {
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            segments = emptyList()
        )
        
        val result = EvaluateFlagUseCase.EvaluationResult(
            variantID = null,
            segmentID = null
        )
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                enableDebug = false
            )
        } returns result
        
        val results = evaluateBatchUseCase.invoke(
            flags = listOf(flag),
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = null,
            enableDebug = false
        )
        
        assertEquals(1, results.size)
        assertEquals(1, results[0].flagID)
        assertNull(results[0].result.variantID)
        
        verify {
            evaluateFlagUseCase.invoke(
                flag,
                match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == null
                },
                false
            )
        }
    }
    
    @Test
    fun testInvoke_PassesEntityContext_ToEvaluateFlagUseCase() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        val entityContext = mapOf("region" to "US", "age" to 25)
        
        val result = EvaluateFlagUseCase.EvaluationResult(
            variantID = 1,
            segmentID = 1
        )
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == entityContext
                },
                enableDebug = false
            )
        } returns result
        
        val results = evaluateBatchUseCase.invoke(
            flags = listOf(flag),
            entityID = EntityID("user123"),
            entityType = "user",
            entityContext = entityContext,
            enableDebug = false
        )
        
        assertEquals(1, results.size)
        verify {
            evaluateFlagUseCase.invoke(
                flag = flag,
                context = match { 
                    it.entityID.value == "user123" &&
                    it.entityType == "user" &&
                    it.entityContext == entityContext
                },
                enableDebug = false
            )
        }
    }
    
    @Test
    fun testInvoke_PassesEnableDebug_ToEvaluateFlagUseCase() {
        val variant = Variant(
            id = 1,
            flagId = 1,
            key = "variant1"
        )
        
        val distribution = Distribution(
            id = 1,
            segmentId = 1,
            variantId = 1,
            percent = 100
        )
        
        val segment = Segment(
            id = 1,
            flagId = 1,
            description = "Segment 1",
            rank = 1,
            rolloutPercent = 100,
            constraints = emptyList(),
            distributions = listOf(distribution)
        )
        
        val flag = Flag(
            id = 1,
            key = "flag1",
            description = "Flag 1",
            enabled = true,
            segments = listOf(segment),
            variants = listOf(variant)
        )
        
        val result = EvaluateFlagUseCase.EvaluationResult(
            variantID = 1,
            segmentID = 1
        )
        
        every {
            evaluateFlagUseCase.invoke(
                flag = flag,
                context = match {
                    it.entityID.value == "user123" &&
                    it.entityType == null &&
                    it.entityContext == null
                },
                enableDebug = true
            )
        } returns result
        
        val results = evaluateBatchUseCase.invoke(
            flags = listOf(flag),
            entityID = EntityID("user123"),
            entityType = null,
            entityContext = null,
            enableDebug = true
        )
        
        assertEquals(1, results.size)
        verify {
            evaluateFlagUseCase.invoke(
                flag,
                match {
                    it.entityID.value == "user123" &&
                    it.entityType == null &&
                    it.entityContext == null
                },
                true
            )
        }
    }
}
