package flagent.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Main App component
 */
@Composable
fun App() {
    // Initialize router on first composition
    LaunchedEffect(Unit) {
        Router.initialize()
    }

    Div({
        style {
            fontFamily("-apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif")
            minHeight(100.vh)
            backgroundColor(FlagentTheme.BackgroundAlt)
        }
    }) {
        // Navbar
        Navbar()
        
        // Main content
        Div({
            style {
                maxWidth(1200.px)
                property("margin", "0 auto")
                padding(20.px)
                property("width", "100%")
                property("box-sizing", "border-box")
            }
        }) {
            // Breadcrumbs (only show on detail pages)
            when (Router.currentRoute) {
                is Route.FlagDetail, is Route.CreateFlag, is Route.DebugConsole, is Route.FlagHistory -> {
                    Breadcrumbs()
                }
                else -> {}
            }
            
            // Route content
            when (val route = Router.currentRoute) {
                is Route.Home -> FlagsList()
                is Route.FlagDetail -> FlagEditor(route.flagId)
                is Route.CreateFlag -> FlagEditor(null)
                is Route.DebugConsole -> DebugConsole(route.flagKey)
                is Route.FlagHistory -> FlagHistory(route.flagId)
            }
        }
    }
}
