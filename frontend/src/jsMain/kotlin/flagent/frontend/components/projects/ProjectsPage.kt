package flagent.frontend.components.projects

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.ApplicationResponse
import flagent.frontend.api.InstanceResponse
import flagent.frontend.api.ProjectResponse
import flagent.frontend.components.Icon
import flagent.frontend.components.common.PageHeader
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.navigation.Route
import flagent.frontend.navigation.Router
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Projects / Applications / Instances page (Enterprise).
 * Hierarchy: Projects list -> Project detail (applications) -> Application detail (instances).
 */
@Composable
fun ProjectsPage() {
    val route = Router.currentRoute
    when (route) {
        is Route.Projects -> ProjectsListView()
        is Route.ProjectDetail -> ApplicationsListView(route.projectId)
        is Route.ApplicationDetail -> InstancesListView(route.projectId, route.appId)
        else -> ProjectsListView()
    }
}

@Composable
private fun ProjectsListView() {
    val themeMode = LocalThemeMode.current
    var projects by remember { mutableStateOf<List<ProjectResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var createKey by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = { projects = ApiClient.getProjects() },
            onError = { err -> error = ErrorHandler.getUserMessage(err) }
        )
        isLoading = false
    }

    Div({ style { padding(0.px) } }) {
        PageHeader(
            title = "Projects",
            subtitle = "Organize flags by product or team. Create projects, then add applications and instances."
        )
        if (isLoading) {
            Div({ style { textAlign("center"); padding(40.px) } }) { Text(LocalizedStrings.loading) }
        } else if (error != null) {
            Div({
                style {
                    padding(20.px)
                    backgroundColor(FlagentTheme.Error)
                    borderRadius(8.px)
                    color(Color.white)
                }
            }) { Text(error!!) }
        } else {
            Button({
                style {
                    padding(10.px, 16.px)
                    backgroundColor(FlagentTheme.Primary)
                    color(Color.white)
                    border(0.px)
                    borderRadius(6.px)
                    cursor("pointer")
                    fontSize(14.px)
                }
                onClick { showCreate = true; createName = ""; createKey = "" }
            }) {
                Icon("add", size = 18.px, color = FlagentTheme.Background)
                Text(" New project")
            }
            if (showCreate) {
                Div({
                    style {
                        marginTop(16.px)
                        padding(16.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                    }
                }) {
                    Text("Name"); Br()
                    Input(InputType.Text, {
                        value(createName)
                        onInput { createName = it.value }
                        style { width(100.percent); maxWidth(300.px); padding(8.px); marginBottom(8.px) }
                    })
                    Br()
                    Text("Key"); Br()
                    Input(InputType.Text, {
                        value(createKey)
                        onInput { createKey = it.value }
                        style { width(100.percent); maxWidth(300.px); padding(8.px); marginBottom(8.px) }
                    })
                    Br()
                    Button({
                        onClick {
                            scope.launch {
                                ErrorHandler.withErrorHandling(
                                    block = {
                                        ApiClient.createProject(createName.trim(), createKey.trim().ifBlank { createName.trim().lowercase().replace(" ", "_") })
                                        projects = ApiClient.getProjects()
                                        showCreate = false
                                    },
                                    onError = { err -> error = ErrorHandler.getUserMessage(err) }
                                )
                            }
                        }
                        style { padding(8.px, 16.px); backgroundColor(FlagentTheme.Primary); color(Color.white); border(0.px); borderRadius(6.px); cursor("pointer"); marginRight(8.px) }
                    }) { Text("Create") }
                    Button({
                        onClick { showCreate = false }
                        style { padding(8.px, 16.px); backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode)); borderRadius(6.px); cursor("pointer") }
                    }) { Text("Cancel") }
                }
            }
            if (projects.isEmpty() && !showCreate) {
                Div({
                    style {
                        marginTop(24.px)
                        padding(40.px)
                        backgroundColor(FlagentTheme.cardBg(themeMode))
                        borderRadius(8.px)
                        textAlign("center")
                    }
                }) {
                    Icon("folder", size = 48.px, color = FlagentTheme.textLight(themeMode))
                    P({ style { marginTop(16.px); color(FlagentTheme.text(themeMode)); fontSize(16.px) } }) { Text("No projects yet") }
                    Text("Create a project to group applications and instances.")
                }
            } else {
                Div({
                    style {
                        marginTop(16.px)
                        display(DisplayStyle.Grid)
                        property("grid-template-columns", "repeat(auto-fill, minmax(260px, 1fr))")
                        gap(16.px)
                    }
                }) {
                    projects.forEach { p ->
                        Div({
                            style {
                                backgroundColor(FlagentTheme.cardBg(themeMode))
                                borderRadius(8.px)
                                padding(16.px)
                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                cursor("pointer")
                            }
                            onClick { Router.navigateTo(Route.ProjectDetail(p.id)) }
                        }) {
                            H3({ style { fontSize(16.px); margin(0.px); color(FlagentTheme.Primary) } }) { Text(p.name) }
                            Span({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)) } }) { Text(p.key) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationsListView(projectId: Long) {
    val themeMode = LocalThemeMode.current
    var project by remember { mutableStateOf<ProjectResponse?>(null) }
    var applications by remember { mutableStateOf<List<ApplicationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                project = ApiClient.getProject(projectId)
                applications = ApiClient.getApplications(projectId)
            },
            onError = { err -> error = ErrorHandler.getUserMessage(err) }
        )
        isLoading = false
    }

    Div({ style { padding(0.px) } }) {
        Div({
            style { marginBottom(16.px); display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px) }
            onClick { Router.navigateTo(Route.Projects) }
        }) {
            Span({ style { color(FlagentTheme.textLight(themeMode)); cursor("pointer"); fontSize(14.px) } }) { Text("Projects") }
            Span({ style { color(FlagentTheme.textLight(themeMode)) } }) { Text(" / ") }
            Span({ style { color(FlagentTheme.text(themeMode)); fontWeight("600") } }) { Text(project?.name ?: "…") }
        }
        PageHeader(title = project?.name ?: "Project", subtitle = "Applications (e.g. iOS, Android, Web)")
        if (isLoading) {
            Div({ style { textAlign("center"); padding(40.px) } }) { Text(LocalizedStrings.loading) }
        } else if (error != null) {
            Div({ style { padding(20.px); backgroundColor(FlagentTheme.Error); borderRadius(8.px); color(Color.white) } }) { Text(error!!) }
        } else {
            if (applications.isEmpty()) {
                Div({ style { padding(24.px); backgroundColor(FlagentTheme.cardBg(themeMode)); borderRadius(8.px); textAlign("center") } }) {
                    Text("No applications. Use API to add applications to this project.")
                }
            } else {
                Div({ style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(12.px) } }) {
                    applications.forEach { app ->
                        Div({
                            style {
                                backgroundColor(FlagentTheme.cardBg(themeMode))
                                borderRadius(8.px)
                                padding(16.px)
                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                                cursor("pointer")
                            }
                            onClick { Router.navigateTo(Route.ApplicationDetail(projectId, app.id)) }
                        }) {
                            H3({ style { fontSize(16.px); margin(0.px); color(FlagentTheme.Primary) } }) { Text(app.name) }
                            Span({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)) } }) { Text("${app.key} · ${app.platform}") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstancesListView(projectId: Long, appId: Long) {
    val themeMode = LocalThemeMode.current
    var project by remember { mutableStateOf<ProjectResponse?>(null) }
    var application by remember { mutableStateOf<ApplicationResponse?>(null) }
    var instances by remember { mutableStateOf<List<InstanceResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId, appId) {
        isLoading = true
        error = null
        ErrorHandler.withErrorHandling(
            block = {
                project = ApiClient.getProject(projectId)
                application = ApiClient.getApplications(projectId).find { it.id == appId }
                instances = ApiClient.getInstances(projectId, appId)
            },
            onError = { err -> error = ErrorHandler.getUserMessage(err) }
        )
        isLoading = false
    }

    Div({ style { padding(0.px) } }) {
        Div({
            style { marginBottom(16.px); display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); flexWrap(FlexWrap.Wrap) }
        }) {
            Span({
                style { color(FlagentTheme.textLight(themeMode)); cursor("pointer"); fontSize(14.px) }
                onClick { Router.navigateTo(Route.Projects) }
            }) { Text("Projects") }
            Span({ style { color(FlagentTheme.textLight(themeMode)) } }) { Text(" / ") }
            Span({
                style { color(FlagentTheme.text(themeMode)); cursor("pointer") }
                onClick { Router.navigateTo(Route.ProjectDetail(projectId)) }
            }) { Text(project?.name ?: "…") }
            Span({ style { color(FlagentTheme.textLight(themeMode)) } }) { Text(" / ") }
            Span({ style { color(FlagentTheme.text(themeMode)); fontWeight("600") } }) { Text(application?.name ?: "…") }
        }
        PageHeader(title = application?.name ?: "Application", subtitle = "Instances (application + environment)")
        if (isLoading) {
            Div({ style { textAlign("center"); padding(40.px) } }) { Text(LocalizedStrings.loading) }
        } else if (error != null) {
            Div({ style { padding(20.px); backgroundColor(FlagentTheme.Error); borderRadius(8.px); color(Color.white) } }) { Text(error!!) }
        } else {
            if (instances.isEmpty()) {
                Div({ style { padding(24.px); backgroundColor(FlagentTheme.cardBg(themeMode)); borderRadius(8.px); textAlign("center") } }) {
                    Text("No instances. Use API to add instances (application + environment).")
                }
            } else {
                Div({ style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(12.px) } }) {
                    instances.forEach { inst ->
                        Div({
                            style {
                                backgroundColor(FlagentTheme.cardBg(themeMode))
                                borderRadius(8.px)
                                padding(16.px)
                                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                            }
                        }) {
                            H3({ style { fontSize(16.px); margin(0.px); color(FlagentTheme.text(themeMode)) } }) { Text(inst.name) }
                            Span({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)) } }) { Text("env id: ${inst.environmentId}") }
                        }
                    }
                }
            }
        }
    }
}
