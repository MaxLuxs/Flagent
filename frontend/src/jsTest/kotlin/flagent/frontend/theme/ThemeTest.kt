package flagent.frontend.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for FlagentTheme
 */
class ThemeTest {
    @Test
    fun testPrimaryColors() {
        assertNotNull(FlagentTheme.Primary)
        assertNotNull(FlagentTheme.PrimaryDark)
        assertNotNull(FlagentTheme.PrimaryLight)
        
        assertEquals("#0EA5E9", FlagentTheme.Primary.toString())
        assertEquals("#0284C7", FlagentTheme.PrimaryDark.toString())
        assertEquals("#38BDF8", FlagentTheme.PrimaryLight.toString())
    }
    
    @Test
    fun testSecondaryColors() {
        assertNotNull(FlagentTheme.Secondary)
        assertNotNull(FlagentTheme.SecondaryDark)
        assertNotNull(FlagentTheme.SecondaryLight)
        
        assertEquals("#14B8A6", FlagentTheme.Secondary.toString())
        assertEquals("#0D9488", FlagentTheme.SecondaryDark.toString())
        assertEquals("#5EEAD4", FlagentTheme.SecondaryLight.toString())
    }
    
    @Test
    fun testBackgroundColors() {
        assertNotNull(FlagentTheme.Background)
        assertNotNull(FlagentTheme.BackgroundAlt)
        assertNotNull(FlagentTheme.BackgroundDark)
        
        assertEquals("#FFFFFF", FlagentTheme.Background.toString())
        assertEquals("#F8FAFC", FlagentTheme.BackgroundAlt.toString())
        assertEquals("#F1F5F9", FlagentTheme.BackgroundDark.toString())
    }
    
    @Test
    fun testTextColors() {
        assertNotNull(FlagentTheme.Text)
        assertNotNull(FlagentTheme.TextLight)
        assertNotNull(FlagentTheme.TextLighter)
        
        assertEquals("#0F172A", FlagentTheme.Text.toString())
        assertEquals("#64748B", FlagentTheme.TextLight.toString())
        assertEquals("#94A3B8", FlagentTheme.TextLighter.toString())
    }
    
    @Test
    fun testStatusColors() {
        assertNotNull(FlagentTheme.Success)
        assertNotNull(FlagentTheme.Error)
        assertNotNull(FlagentTheme.Warning)
        assertNotNull(FlagentTheme.Info)
        
        assertEquals("#10B981", FlagentTheme.Success.toString())
        assertEquals("#EF4444", FlagentTheme.Error.toString())
        assertEquals("#F59E0B", FlagentTheme.Warning.toString())
        assertEquals("#3B82F6", FlagentTheme.Info.toString())
    }
    
    @Test
    fun testNeutralColors() {
        assertNotNull(FlagentTheme.Neutral)
        assertNotNull(FlagentTheme.NeutralLight)
        assertNotNull(FlagentTheme.NeutralLighter)
        
        assertEquals("#6B7280", FlagentTheme.Neutral.toString())
        assertEquals("#9CA3AF", FlagentTheme.NeutralLight.toString())
        assertEquals("#D1D5DB", FlagentTheme.NeutralLighter.toString())
    }
    
    @Test
    fun testShadowValues() {
        assertNotNull(FlagentTheme.Shadow)
        assertNotNull(FlagentTheme.ShadowHover)
        
        assertEquals("rgba(0, 0, 0, 0.08)", FlagentTheme.Shadow)
        assertEquals("rgba(0, 0, 0, 0.15)", FlagentTheme.ShadowHover)
    }
}
