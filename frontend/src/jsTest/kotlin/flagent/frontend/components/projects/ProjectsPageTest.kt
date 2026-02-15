package flagent.frontend.components.projects

import flagent.frontend.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectsPageTest {

    @Test
    fun projectDetailPath_format() {
        val r = Route.ProjectDetail(5L)
        assertEquals("/projects/5", r.path())
    }

    @Test
    fun applicationDetailPath_format() {
        val r = Route.ApplicationDetail(1L, 10L)
        assertEquals("/projects/1/applications/10", r.path())
    }
}
