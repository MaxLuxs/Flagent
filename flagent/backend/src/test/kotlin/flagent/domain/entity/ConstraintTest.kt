package flagent.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConstraintTest {
    @Test
    fun `toExprStr converts constraint to expression string`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "age",
            operator = "GT",
            value = "18"
        )
        
        val expr = constraint.toExprStr()
        
        assertEquals("({age} > 18)", expr)
    }
    
    @Test
    fun `toExprStr throws exception for unsupported operator`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "age",
            operator = "UNSUPPORTED",
            value = "18"
        )
        
        assertFailsWith<IllegalArgumentException> {
            constraint.toExprStr()
        }
    }
    
    @Test
    fun `toExprStr throws exception for empty fields`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "",
            operator = "GT",
            value = "18"
        )
        
        assertFailsWith<IllegalArgumentException> {
            constraint.toExprStr()
        }
    }
    
    @Test
    fun `validate passes for valid constraint`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "age",
            operator = "GT",
            value = "18"
        )
        
        constraint.validate() // Should not throw
    }
    
    @Test
    fun `validate throws exception for invalid constraint`() {
        val constraint = Constraint(
            id = 1,
            segmentId = 1,
            property = "",
            operator = "GT",
            value = "18"
        )
        
        assertFailsWith<IllegalArgumentException> {
            constraint.validate()
        }
    }
    
    @Test
    fun `all operators are mapped correctly`() {
        val operators = listOf("EQ", "NEQ", "LT", "LTE", "GT", "GTE", "EREG", "NEREG", "IN", "NOTIN", "CONTAINS", "NOTCONTAINS")
        
        operators.forEach { operator ->
            val constraint = Constraint(
                id = 1,
                segmentId = 1,
                property = "test",
                operator = operator,
                value = "value"
            )
            
            constraint.validate() // Should not throw
            constraint.toExprStr() // Should not throw
        }
    }
}
