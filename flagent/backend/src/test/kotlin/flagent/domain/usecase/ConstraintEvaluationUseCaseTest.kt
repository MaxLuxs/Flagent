package flagent.domain.usecase

import flagent.domain.entity.Constraint
import flagent.domain.value.EntityID
import flagent.domain.value.EvaluationContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstraintEvaluationUseCaseTest {
    private val useCase = ConstraintEvaluationUseCase()
    
    @Test
    fun `evaluate returns true for empty constraints`() {
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25)
        )
        
        val result = useCase.evaluate(emptyList(), context)
        
        assertTrue(result)
    }
    
    @Test
    fun `evaluate returns false when entity context is null`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "GT",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123")
        )
        
        val result = useCase.evaluate(constraints, context)
        
        assertFalse(result)
    }
    
    @Test
    fun `evaluate EQ operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "country",
                operator = "EQ",
                value = "US"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "US")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "CA")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NEQ operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "country",
                operator = "NEQ",
                value = "US"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "CA")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "US")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate LT operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "LT",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 17)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 18)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate GT operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "GT",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 19)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 18)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate IN operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "country",
                operator = "IN",
                value = "US,CA,MX"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "US")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "UK")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate CONTAINS operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "CONTAINS",
                value = "@example.com"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@other.com")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate EREG operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "EREG",
                value = "^[a-z]+@example\\.com$"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@other.com")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate multiple constraints - all must match`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "GT",
                value = "18"
            ),
            Constraint(
                id = 2,
                segmentId = 1,
                property = "country",
                operator = "EQ",
                value = "US"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25, "country" to "US")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 25, "country" to "CA")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate LTE operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "LTE",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 18)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 19)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate GTE operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "GTE",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 18)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 17)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NOTIN operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "country",
                operator = "NOTIN",
                value = "US,CA,MX"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "UK")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("country" to "US")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NOTCONTAINS operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "NOTCONTAINS",
                value = "@example.com"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@other.com")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NEREG operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "NEREG",
                value = "^[a-z]+@example\\.com$"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@other.com")
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate returns false for unknown operator`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "UNKNOWN",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to 18)
        )
        
        assertFalse(useCase.evaluate(constraints, context))
    }
    
    @Test
    fun `evaluate returns false when property is missing`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "missing_property",
                operator = "EQ",
                value = "value"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("other_property" to "value")
        )
        
        assertFalse(useCase.evaluate(constraints, context))
    }
    
    @Test
    fun `evaluate handles numeric comparison with non-numeric values`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "age",
                operator = "GT",
                value = "18"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("age" to "not-a-number")
        )
        
        // Should return false when comparison fails
        assertFalse(useCase.evaluate(constraints, context))
    }
    
    @Test
    fun `evaluate handles invalid regex`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "EREG",
                value = "[invalid regex"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        // Should return false when regex is invalid
        assertFalse(useCase.evaluate(constraints, context))
    }
    
    @Test
    fun `evaluate LT operator with floating point numbers`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "score",
                operator = "LT",
                value = "10.5"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.4)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.6)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate GT operator with floating point numbers`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "score",
                operator = "GT",
                value = "10.5"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.6)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.4)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate LTE operator with floating point numbers`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "score",
                operator = "LTE",
                value = "10.5"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.5)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.6)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate GTE operator with floating point numbers`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "score",
                operator = "GTE",
                value = "10.5"
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.5)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("score" to 10.4)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate EREG operator with various regex patterns`() {
        // Test email pattern
        val constraints1 = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "email",
                operator = "EREG",
                value = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            )
        )
        
        val context1 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "user@example.com")
        )
        
        assertTrue(useCase.evaluate(constraints1, context1))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("email" to "invalid-email")
        )
        
        assertFalse(useCase.evaluate(constraints1, context2))
        
        // Test phone number pattern
        val constraints2 = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "phone",
                operator = "EREG",
                value = "^\\+?[1-9]\\d{1,14}$"
            )
        )
        
        val context3 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("phone" to "+1234567890")
        )
        
        assertTrue(useCase.evaluate(constraints2, context3))
    }
    
    @Test
    fun `evaluate IN operator with large number of values`() {
        val largeList = (1..100).joinToString(",")
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "id",
                operator = "IN",
                value = largeList
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("id" to 50)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("id" to 101)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NOTIN operator with large number of values`() {
        val largeList = (1..100).joinToString(",")
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "id",
                operator = "NOTIN",
                value = largeList
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("id" to 101)
        )
        
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("id" to 50)
        )
        
        assertFalse(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate CONTAINS operator with empty strings`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "text",
                operator = "CONTAINS",
                value = ""
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("text" to "any text")
        )
        
        // Empty string should be contained in any string
        assertTrue(useCase.evaluate(constraints, context))
        
        val context2 = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("text" to "")
        )
        
        // Empty string contains empty string
        assertTrue(useCase.evaluate(constraints, context2))
    }
    
    @Test
    fun `evaluate NOTCONTAINS operator with empty strings`() {
        val constraints = listOf(
            Constraint(
                id = 1,
                segmentId = 1,
                property = "text",
                operator = "NOTCONTAINS",
                value = ""
            )
        )
        
        val context = EvaluationContext(
            entityID = EntityID("user123"),
            entityContext = mapOf("text" to "any text")
        )
        
        // Empty string is always contained, so NOTCONTAINS should be false
        assertFalse(useCase.evaluate(constraints, context))
    }
}
