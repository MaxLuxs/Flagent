package flagent.frontend.i18n

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for LocalizedStrings used in login, segments, experiments, analytics, and crash UI.
 */
class LocalizedStringsTest {

    @Test
    fun supportLinkNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.supportLink.isNotBlank(), "supportLink should be non-blank for $locale")
        }
    }

    @Test
    fun copyLabelNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.copyLabel.isNotBlank(), "copyLabel should be non-blank for $locale")
        }
    }

    @Test
    fun noExperimentsMatchFilterNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.noExperimentsMatchFilter.isNotBlank(), "noExperimentsMatchFilter should be non-blank for $locale")
        }
    }

    @Test
    fun filterAllNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.filterAll.isNotBlank(), "filterAll should be non-blank for $locale")
        }
    }

    @Test
    fun searchSegmentsPlaceholderNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.searchSegmentsPlaceholder.isNotBlank(), "searchSegmentsPlaceholder should be non-blank for $locale")
        }
    }

    @Test
    fun noSegmentsMatchSearchNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.noSegmentsMatchSearch.isNotBlank(), "noSegmentsMatchSearch should be non-blank for $locale")
        }
    }

    @Test
    fun noAnalyticsEventsYetNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.noAnalyticsEventsYet.isNotBlank(), "noAnalyticsEventsYet should be non-blank for $locale")
        }
    }

    @Test
    fun viewCrashRateNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.viewCrashRate.isNotBlank(), "viewCrashRate should be non-blank for $locale")
        }
    }

    @Test
    fun crashAnalyticsDescriptionNonBlank() {
        listOf("ru", "en").forEach { locale ->
            LocalizedStrings.currentLocale = locale
            assertTrue(LocalizedStrings.crashAnalyticsDescription.isNotBlank(), "crashAnalyticsDescription should be non-blank for $locale")
        }
    }
}
