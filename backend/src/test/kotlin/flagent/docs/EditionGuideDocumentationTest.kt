package flagent.docs

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue
import java.io.File

/**
 * Verifies that EDITION_GUIDE.md contains the OSS auth enablement section
 * added as part of the landing page and navigation improvements.
 */
class EditionGuideDocumentationTest {

    @Test
    fun editionGuideContainsEnablingAuthInOpenSourceSection() {
        val editionGuide = File("../frontend/EDITION_GUIDE.md")
        assertTrue(editionGuide.exists(), "EDITION_GUIDE.md should exist")
        val content = editionGuide.readText()
        assertContains(
            content,
            "Authentication in Open Source",
            ignoreCase = false,
            message = "EDITION_GUIDE must document auth in OSS"
        )
        assertContains(
            content,
            "FLAGENT_ADMIN_AUTH_ENABLED",
            ignoreCase = false,
            message = "EDITION_GUIDE must mention FLAGENT_ADMIN_AUTH_ENABLED"
        )
        assertContains(
            content,
            "ENV_FEATURE_AUTH",
            ignoreCase = false,
            message = "EDITION_GUIDE must mention ENV_FEATURE_AUTH"
        )
    }
}
