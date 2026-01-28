package flagent.frontend.viewmodel

import flagent.frontend.api.MetricType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MetricsViewModelTest {
    
    @Test
    fun testInitialState() {
        val viewModel = MetricsViewModel(flagId = 1)
        
        assertEquals(0, viewModel.metrics.size)
        assertNull(viewModel.aggregation)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
        assertNull(viewModel.selectedMetricType)
        assertNull(viewModel.selectedVariantId)
    }
    
    @Test
    fun testSetMetricType() {
        val viewModel = MetricsViewModel(flagId = 1)
        
        viewModel.selectedMetricType = MetricType.SUCCESS_RATE
        assertEquals(MetricType.SUCCESS_RATE, viewModel.selectedMetricType)
        
        viewModel.selectedMetricType = MetricType.ERROR_RATE
        assertEquals(MetricType.ERROR_RATE, viewModel.selectedMetricType)
    }
    
    @Test
    fun testSetVariantId() {
        val viewModel = MetricsViewModel(flagId = 1)
        
        viewModel.selectedVariantId = 5
        assertEquals(5, viewModel.selectedVariantId)
    }
    
    @Test
    fun testClearError() {
        val viewModel = MetricsViewModel(flagId = 1)
        viewModel.error = "Test error"
        
        assertEquals("Test error", viewModel.error)
        
        viewModel.clearError()
        assertNull(viewModel.error)
    }
}
