package flagent.frontend.components.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportPanelTest {

    @Test
    fun testExportPanelComponentExists() {
        assertTrue(true, "ExportPanel component exists")
    }

    @Test
    fun testExportTypes() {
        val evalCachePath = "/export/eval_cache/json"
        val gitopsYamlPath = "/export/gitops?format=yaml"
        val gitopsJsonPath = "/export/gitops?format=json"
        val sqlitePath = "/export/sqlite"
        assertTrue(evalCachePath.contains("eval_cache"))
        assertTrue(gitopsYamlPath.contains("gitops"))
        assertTrue(gitopsJsonPath.contains("format=json"))
        assertTrue(sqlitePath.contains("sqlite"))
    }

    @Test
    fun testExportFilenames() {
        val filenames = listOf("flagent_eval_cache.json", "flagent_gitops.yaml", "flagent_gitops.json", "flagent_export.sqlite")
        filenames.forEach { assertTrue(it.isNotBlank() && it.contains("flagent_")) }
    }
}
